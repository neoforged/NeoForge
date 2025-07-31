package net.neoforged.neodev.extensionvalidation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.neoforged.neodev.utils.AsmUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
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
import java.util.stream.Collectors;

public abstract class CheckExtensions extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getInput();

    @InputFile
    public abstract RegularFileProperty getDefinitions();

    @TaskAction
    public void exec() throws IOException {
        Set<ExtensionDefinition> definitions = loadDefinitions();

        Map<String, Set<String>> expectedOriginals = new HashMap<>();
        Map<String, Set<String>> expectedReplacements = new HashMap<>();
        for (ExtensionDefinition definition : definitions) {
            MethodDesc original = definition.original;
            expectedOriginals.computeIfAbsent(original.owner, $ -> new HashSet<>())
                    .add(original.name + original.descriptor);
            MethodDesc replacement = definition.replacement;
            expectedReplacements.computeIfAbsent(replacement.owner, $ -> new HashSet<>())
                    .add(replacement.name + replacement.descriptor);
        }

        ValidatingVisitor validator = new ValidatingVisitor(expectedOriginals, expectedReplacements);
        AsmUtils.visitAllClasses(getInput().getAsFile().get(), validator, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        Map<MethodDesc, Set<MethodDesc>> permittedOriginals = new HashMap<>();
        Map<MethodDesc, MethodDesc> replacements = new HashMap<>();
        for (ExtensionDefinition definition : definitions) {
            for (MethodDesc original : resolveAlternateCallees(validator.inheritors, definition.original)) {
                permittedOriginals.computeIfAbsent(definition.replacement, $ -> new HashSet<>()).add(original);
                for (MethodDesc exclusion : definition.exclusions) {
                    permittedOriginals.computeIfAbsent(exclusion, $ -> new HashSet<>())
                            .add(original);
                }
                replacements.put(original, definition.replacement);
            }
        }

        Map<String, Set<String>> missingOriginals = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : expectedOriginals.entrySet()) {
            Set<String> found = validator.locatedOriginals.get(entry.getKey());
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
        Map<String, Set<String>> missingReplacements = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : expectedReplacements.entrySet()) {
            Set<String> found = validator.locatedReplacements.get(entry.getKey());
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
        Map<MethodDesc, Set<MethodDesc>> unreplacedTargets = validator.unreplacedTargets
                .entrySet()
                .stream()
                .peek(entry -> entry.getValue().removeIf(desc ->
                        permittedOriginals.getOrDefault(entry.getKey(), Set.of()).contains(desc) ||
                        !replacements.containsKey(desc)))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (missingOriginals.isEmpty() && missingReplacements.isEmpty() && unreplacedTargets.isEmpty()) return;

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
                        }
                        String callerDesc = caller.name + caller.descriptor;
                        if (!callerDesc.equals(lastClassAndMethod[1])) {
                            builder.append("\t\t\t- ").append(callerDesc).append("\n");
                            lastClassAndMethod[1] = callerDesc;
                        }

                        for (MethodDesc callee : entry.getValue()) {
                            MethodDesc replacement = replacements.get(callee);
                            builder.append("\t\t\t\t- Found: ")
                                    .append(callee.owner)
                                    .append(" ")
                                    .append(callee.name)
                                    .append(callee.descriptor)
                                    .append(", expected: ")
                                    .append(replacement.owner)
                                    .append(" ")
                                    .append(replacement.name)
                                    .append(replacement.descriptor)
                                    .append("\n");
                        }
                    });
        }
        throw new GradleException(builder.toString());
    }

    private Set<ExtensionDefinition> loadDefinitions() throws IOException {
        Set<ExtensionDefinition> defSet = new LinkedHashSet<>();
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

    private record ExtensionDefinition(MethodDesc original, MethodDesc replacement, Set<MethodDesc> exclusions) implements Serializable {}

    record MethodDesc(String owner, String name, String descriptor) implements Serializable {}

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
}
