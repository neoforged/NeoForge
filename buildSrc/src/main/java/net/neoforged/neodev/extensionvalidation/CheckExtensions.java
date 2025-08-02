package net.neoforged.neodev.extensionvalidation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.neoforged.neodev.utils.AsmUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CheckExtensions extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getInput();

    @InputFile
    @Optional
    public abstract RegularFileProperty getDefinitions();

    @InputFile
    @Optional
    public abstract RegularFileProperty getInterfaceInjections();

    @TaskAction
    public void exec() throws IOException {
        Set<ExtensionDefinition> definitions = loadDefinitionsFromJson();
        definitions.addAll(collectAndResolveAnnotationDefinitions());

        Map<String, Set<String>> expectedOriginals = new HashMap<>();
        Map<String, Set<String>> expectedReplacements = new HashMap<>();
        collectExpectedOriginalsAndReplacements(definitions, expectedOriginals, expectedReplacements);

        ValidatingVisitor validator = new ValidatingVisitor(expectedOriginals, expectedReplacements);
        AsmUtils.visitAllClasses(getInput().getAsFile().get(), validator, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        Map<MethodDesc, Set<MethodDesc>> permittedOriginals = new HashMap<>();
        Map<MethodDesc, MethodDesc> replacements = new HashMap<>();
        resolveReplacementsAndPermittedOriginals(definitions, validator.inheritors, permittedOriginals, replacements);

        Map<String, Set<String>> missingOriginals = collectMissingOriginals(expectedOriginals, validator.locatedOriginals);
        Map<String, Set<String>> missingReplacements = collectMissingReplacements(expectedReplacements, validator.locatedReplacements);
        Map<MethodDesc, Set<MethodDesc>> unreplacedTargets = collectUnreplacedTargets(validator.unreplacedTargets, permittedOriginals, replacements);

        if (!missingOriginals.isEmpty() || !missingReplacements.isEmpty() || !unreplacedTargets.isEmpty()) {
            String message = buildExceptionMessage(missingOriginals, missingReplacements, unreplacedTargets, replacements);
            throw new GradleException(message);
        }
    }

    /**
     * Load {@link ExtensionDefinition}s from the JSON file provided as task input.
     */
    private Set<ExtensionDefinition> loadDefinitionsFromJson() throws IOException {
        Set<ExtensionDefinition> defSet = new LinkedHashSet<>();
        if (!getDefinitions().isPresent()) {
            return defSet;
        }

        try (BufferedReader reader = Files.newBufferedReader(getDefinitions().get().getAsFile().toPath())) {
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            for (JsonElement element : root.getAsJsonArray("extensions").asList()) {
                JsonObject extension = element.getAsJsonObject();

                Set<MethodDesc> exclusions = new HashSet<>();
                if (extension.has("exclusions")) {
                    for (JsonElement excElem : extension.getAsJsonArray("exclusions").asList()) {
                        exclusions.add(loadDescriptor(excElem.getAsJsonObject()));
                    }
                }

                defSet.add(new ExtensionDefinition(
                        loadDescriptor(extension.getAsJsonObject("original")),
                        loadDescriptor(extension.getAsJsonObject("replacement")),
                        exclusions));
            }
        }
        return defSet;
    }

    private static MethodDesc loadDescriptor(JsonObject obj) {
        return new MethodDesc(
                obj.get("owner").getAsString(),
                obj.get("name").getAsString(),
                obj.get("descriptor").getAsString()
        );
    }

    /**
     * Collect {@link ExtensionDefinition}s from {@code @ExtensionMethod} annotations in the code this task operates on
     * and resolve incomplete descriptors.
     *
     * @see CollectingVisitor
     * @see ResolvingVisitor
     */
    private List<ExtensionDefinition> collectAndResolveAnnotationDefinitions() throws IOException {
        Map<String, String> itfInjectTargets = loadInterfaceInjections();

        CollectingVisitor collector = new CollectingVisitor(itfInjectTargets);
        AsmUtils.visitAllClasses(getInput().getAsFile().get(), collector, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE);

        if (collector.definitions.stream().anyMatch(ExtensionDefinition::isIncomplete)) {
            ResolvingVisitor resolver = new ResolvingVisitor(collector.definitions);
            AsmUtils.visitAllClasses(getInput().getAsFile().get(), resolver, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE);
            collector.definitions.replaceAll(resolver::resolve);
        }

        return collector.definitions;
    }

    /**
     * Load interface-injection data to aid in resolving incomplete descriptors from {@code @ExtensionMethod} annotations.
     */
    private Map<String, String> loadInterfaceInjections() throws IOException {
        if (!getInterfaceInjections().isPresent()) {
            return Map.of();
        }

        Set<String> encounteredInterfaces = new HashSet<>();
        Map<String, String> injections = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(getInterfaceInjections().get().getAsFile().toPath())) {
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    String itf = element.getAsString();
                    if (encounteredInterfaces.add(itf)) {
                        injections.put(itf, entry.getKey());
                    } else {
                        injections.remove(itf);
                    }
                }
            }
        }
        return injections;
    }

    /**
     * Collect the method descriptors of expected original and replacement methods on a per-class level for
     * validation in {@link ValidatingVisitor}.
     *
     * @param definitions          The loaded definitions
     * @param expectedOriginals    Map of class names to set of descriptors of original methods expected to be present in the former
     * @param expectedReplacements Map of class names to set of descriptors of replacement methods expected to be present in the former
     */
    private static void collectExpectedOriginalsAndReplacements(
            Set<ExtensionDefinition> definitions,
            Map<String, Set<String>> expectedOriginals,
            Map<String, Set<String>> expectedReplacements
    ) {
        for (ExtensionDefinition definition : definitions) {
            MethodDesc original = definition.original;
            expectedOriginals.computeIfAbsent(original.owner, $ -> new HashSet<>())
                    .add(original.name + original.descriptor);
            MethodDesc replacement = definition.replacement;
            expectedReplacements.computeIfAbsent(replacement.owner, $ -> new HashSet<>())
                    .add(replacement.name + replacement.descriptor);
        }
    }

    /**
     * Resolve all possible callee descriptors for replacement methods and permitted calls to the original methods
     * based on the inheritance data collected by the {@link ValidatingVisitor}.
     *
     * @param definitions        The loaded extension definitions
     * @param inheritors         Map of class names to set of class names inheriting from the former
     * @param permittedOriginals Map of methods to set of original methods the former is permitted to call
     * @param replacements       Map of original methods to replacing extension methods
     */
    private static void resolveReplacementsAndPermittedOriginals(
            Set<ExtensionDefinition> definitions,
            Map<String, Set<String>> inheritors,
            Map<MethodDesc, Set<MethodDesc>> permittedOriginals,
            Map<MethodDesc, MethodDesc> replacements
    ) {
        for (ExtensionDefinition definition : definitions) {
            for (MethodDesc original : resolveAlternateCallees(inheritors, definition.original)) {
                permittedOriginals.computeIfAbsent(definition.replacement, $ -> new HashSet<>()).add(original);
                for (MethodDesc exclusion : definition.exclusions) {
                    permittedOriginals.computeIfAbsent(exclusion, $ -> new HashSet<>())
                            .add(original);
                }
                replacements.put(original, definition.replacement);
            }
        }
    }

    /**
     * Resolve all possible callee descriptors of the provided {@link MethodDesc} based on the inheritance data
     * collected by the {@link ValidatingVisitor}.
     *
     * @param inheritors Map of class names to set of class names inheriting from the former
     * @param original   The {@link MethodDesc} whose possible callee descriptors to resolve
     */
    private static List<MethodDesc> resolveAlternateCallees(Map<String, Set<String>> inheritors, MethodDesc original) {
        List<MethodDesc> callees = new ArrayList<>();
        callees.add(original);
        List<MethodDesc> pending = new ArrayList<>();
        pending.add(original);
        while (!pending.isEmpty()) {
            List<MethodDesc> lastPending = new ArrayList<>(pending);
            pending.clear();
            for (MethodDesc desc : lastPending) {
                Set<String> classes = inheritors.getOrDefault(desc.owner, Set.of());
                for (String clazz : classes) {
                    MethodDesc altDesc = new MethodDesc(clazz, desc.name, desc.descriptor);
                    callees.add(altDesc);
                    pending.add(altDesc);
                }
            }
        }
        return callees;
    }

    /**
     * Collect original methods which were specified in the JSON file or {@code @ExtensionMethod} annotations but not
     * found in the compiled code.
     *
     * @param expectedOriginals Map of class names to set of descriptors of original methods expected to be present in the former
     * @param locatedOriginals  Map of class names to set of descriptors of original methods found in the former
     * @return Map of class names to set of method descriptors of original methods expected but not found in the former
     */
    private static Map<String, Set<String>> collectMissingOriginals(
            Map<String, Set<String>> expectedOriginals,
            Map<String, Set<String>> locatedOriginals
    ) {
        Map<String, Set<String>> missingOriginals = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : expectedOriginals.entrySet()) {
            Set<String> found = locatedOriginals.get(entry.getKey());
            if (found == null) {
                missingOriginals.put(entry.getKey(), entry.getValue());
                continue;
            }

            Set<String> missing = new HashSet<>(entry.getValue());
            missing.removeAll(found);
            if (!missing.isEmpty()) {
                missingOriginals.put(entry.getKey(), missing);
            }
        }
        return missingOriginals;
    }

    /**
     * Collect replacement methods which were specified in the JSON file but not found in the compiled code.
     *
     * @param expectedReplacements Map of class names to set of descriptors of replacement methods expected to be present in the former
     * @param locatedReplacements  Map of class names to set of descriptors of replacement methods found in the former
     * @return Map of class names to set of method descriptors of original methods expected but not found in the former
     */
    private static Map<String, Set<String>> collectMissingReplacements(
            Map<String, Set<String>> expectedReplacements,
            Map<String, Set<String>> locatedReplacements
    ) {
        Map<String, Set<String>> missingReplacements = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : expectedReplacements.entrySet()) {
            Set<String> found = locatedReplacements.get(entry.getKey());
            if (found == null) {
                missingReplacements.put(entry.getKey(), entry.getValue());
                continue;
            }

            Set<String> missing = new HashSet<>(entry.getValue());
            missing.removeAll(found);
            if (!missing.isEmpty()) {
                missingReplacements.put(entry.getKey(), missing);
            }
        }
        return missingReplacements;
    }

    /**
     * Collect unreplaced calls to original methods for which replacements exist and the call-site is not in the list
     * of permitted call-sites.
     *
     * @param unreplacedTargets  Map of calling methods to set of potential original methods called in the former
     * @param permittedOriginals Map of methods to set of original methods the former is permitted to call
     * @param replacements       Map of original methods to replacing extension methods
     * @return Map of calling method to set of original methods called in the former
     */
    private static Map<MethodDesc, Set<MethodDesc>> collectUnreplacedTargets(
            Map<MethodDesc, Set<MethodDesc>> unreplacedTargets,
            Map<MethodDesc, Set<MethodDesc>> permittedOriginals,
            Map<MethodDesc, MethodDesc> replacements
    ) {
        Map<MethodDesc, Set<MethodDesc>> map = new HashMap<>();
        for (Map.Entry<MethodDesc, Set<MethodDesc>> entry : unreplacedTargets.entrySet()) {
            Set<MethodDesc> unreplaced = entry.getValue();
            Set<MethodDesc> permitted = permittedOriginals.getOrDefault(entry.getKey(), Set.of());
            unreplaced.removeIf(desc -> permitted.contains(desc) || !replacements.containsKey(desc));
            if (!unreplaced.isEmpty()) {
                map.put(entry.getKey(), unreplaced);
            }
        }
        return map;
    }

    private static String buildExceptionMessage(
            Map<String, Set<String>> missingOriginals,
            Map<String, Set<String>> missingReplacements,
            Map<MethodDesc, Set<MethodDesc>> unreplacedTargets,
            Map<MethodDesc, MethodDesc> replacements
    ) {
        StringBuilder builder = new StringBuilder("Found issues with extensions:\n");
        if (!missingOriginals.isEmpty()) {
            builder.append("\tMissing/invalid extension targets:\n");
            for (Map.Entry<String, Set<String>> entry : missingOriginals.entrySet()) {
                builder.append("\t\t- ").append(entry.getKey()).append("\n");
                for (String method : entry.getValue()) {
                    builder.append("\t\t\t- ").append(method).append("\n");
                }
            }
        }
        if (!missingReplacements.isEmpty()) {
            builder.append("\tMissing/invalid extension replacements:\n");
            for (Map.Entry<String, Set<String>> entry : missingReplacements.entrySet()) {
                builder.append("\t\t- ").append(entry.getKey()).append("\n");
                for (String method : entry.getValue()) {
                    builder.append("\t\t\t- ").append(method).append("\n");
                }
            }
        }
        if (!unreplacedTargets.isEmpty()) {
            String[] lastClassAndMethod = new String[2];
            builder.append("\tUnreplaced targets:\n");
            unreplacedTargets.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(e -> e.getKey().owner))
                    .forEachOrdered(entry -> {
                        MethodDesc caller = entry.getKey();
                        if (!caller.owner.equals(lastClassAndMethod[0])) {
                            builder.append("\t\t- ").append(caller.owner).append("\n");
                            lastClassAndMethod[0] = caller.owner;
                            lastClassAndMethod[1] = null;
                        }
                        String callerDesc = caller.name + caller.descriptor;
                        if (!callerDesc.equals(lastClassAndMethod[1])) {
                            builder.append("\t\t\t- ").append(callerDesc).append("\n");
                            lastClassAndMethod[1] = callerDesc;
                        }

                        for (MethodDesc callee : entry.getValue()) {
                            MethodDesc replacement = replacements.get(callee);
                            builder.append("\t\t\t\t- Found: ")
                                    .append(callee)
                                    .append(", expected: ")
                                    .append(replacement)
                                    .append("\n");
                        }
                    });
        }
        return builder.toString();
    }

    record ExtensionDefinition(MethodDesc original, MethodDesc replacement, Set<MethodDesc> exclusions) implements Serializable {
        boolean isIncomplete() {
            return original.descriptor == null || exclusions.stream().anyMatch(desc -> desc.descriptor == null);
        }
    }

    record MethodDesc(String owner, String name, String descriptor) implements Serializable {
        @Override
        public String toString() {
            return owner + " " + name + descriptor;
        }
    }
}
