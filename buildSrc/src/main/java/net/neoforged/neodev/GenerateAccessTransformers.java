package net.neoforged.neodev;

import net.neoforged.neodev.utils.FileUtils;
import net.neoforged.neodev.utils.SerializablePredicate;
import net.neoforged.neodev.utils.structure.ClassInfo;
import net.neoforged.neodev.utils.structure.ClassStructureVisitor;
import net.neoforged.neodev.utils.structure.FieldInfo;
import net.neoforged.neodev.utils.structure.MethodInfo;
import org.gradle.api.DefaultTask;
import org.gradle.api.Named;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This task is used to generate access transformers based on a set of rules defined in the buildscript.
 */
public abstract class GenerateAccessTransformers extends DefaultTask {
    public static final Modifier PUBLIC = new Modifier("public", false, Opcodes.ACC_PUBLIC);
    public static final Modifier PROTECTED = new Modifier("protected", false, Opcodes.ACC_PUBLIC, Opcodes.ACC_PROTECTED);

    @InputFile
    public abstract RegularFileProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getAccessTransformer();

    @Input
    public abstract ListProperty<AtGroup> getGroups();

    @TaskAction
    public void exec() throws IOException {
        // First we collect all classes
        var targets = ClassStructureVisitor.readJar(getInput().getAsFile().get());

        var groupList = getGroups().get();

        List<String>[] groups = new List[groupList.size()];
        for (int i = 0; i < groupList.size(); i++) {
            groups[i] = new ArrayList<>();
        }

        // Now we check each class against each group and see if the group wants to handle it
        for (ClassInfo value : targets.values()) {
            for (int i = 0; i < groupList.size(); i++) {
                var group = groupList.get(i);
                if (group.classMatch.test(value)) {
                    var lastInner = value.name().lastIndexOf("$");
                    // Skip anonymous classes
                    if (lastInner >= 0 && Character.isDigit(value.name().charAt(lastInner + 1))) {
                        continue;
                    }

                    // fieldMatch is non-null only for field ATs
                    if (group.fieldMatch != null) {
                        for (var field : value.fields()) {
                            if (group.fieldMatch.test(field) && !group.modifier.test(field.access())) {
                                groups[i].add(group.modifier.name + " " + value.name().replace('/', '.') + " " + field.name());
                            }
                        }
                    }
                    // methodMatch is non-null only for group ATs
                    else if (group.methodMatch != null) {
                        for (var method : value.methods()) {
                            if (group.methodMatch.test(method) && !group.modifier.test(method.access())) {
                                groups[i].add(group.modifier.name + " " + value.name().replace('/', '.') + " " + method.name() + method.descriptor());
                            }
                        }
                    }
                    // If there's neither a field nor a method predicate, this is a class AT
                    else if (!group.modifier.test(value.access().intValue())) {
                        groups[i].add(group.modifier.name + " " + value.name().replace('/', '.'));

                        // If we AT a record we must ensure that its constructors have the same AT
                        if (value.hasSuperclass("java/lang/Record")) {
                            for (MethodInfo method : value.methods()) {
                                if (method.name().equals("<init>")) {
                                    groups[i].add(group.modifier.name + " " + value.name().replace('/', '.') + " " + method.name() + method.descriptor());
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dump the ATs
        var text = new StringBuilder();

        text.append("# This file is generated based on the rules defined in the buildscript. DO NOT modify it manually.\n# Add more rules in the buildscript and then run the generateAccessTransformers task to update this file.\n\n");

        for (int i = 0; i < groups.length; i++) {
            // Check if the group found no targets. If it didn't, there's probably an error in the test and it should be reported
            if (groups[i].isEmpty()) {
                throw new IllegalStateException("Generated AT group '" + groupList.get(i).name + "' found no entries!");
            }
            text.append("# ").append(groupList.get(i).name).append('\n');
            text.append(groups[i].stream().sorted().collect(Collectors.joining("\n")));
            text.append('\n');

            if (i < groups.length - 1) text.append('\n');
        }

        var outFile = getAccessTransformer().getAsFile().get().toPath();
        if (!Files.exists(outFile.getParent())) {
            Files.createDirectories(outFile.getParent());
        }

        FileUtils.writeStringSafe(outFile, text.toString(), StandardCharsets.UTF_8);
    }

    public void classGroup(String name, Modifier modifier, SerializablePredicate<ClassInfo> match) {
        getGroups().add(new AtGroup(name, modifier, match, null, null));
    }

    public void methodGroup(String name, Modifier modifier, SerializablePredicate<ClassInfo> targetTest, SerializablePredicate<MethodInfo> methodTest) {
        getGroups().add(new AtGroup(name, modifier, targetTest, methodTest, null));
    }

    public void fieldGroup(String name, Modifier modifier, SerializablePredicate<ClassInfo> targetTest, SerializablePredicate<FieldInfo> fieldTest) {
        getGroups().add(new AtGroup(name, modifier, targetTest, null, fieldTest));
    }

    public <T extends Named> SerializablePredicate<T> named(String name) {
        return target -> target.getName().equals(name);
    }

    public SerializablePredicate<ClassInfo> classesWithSuperclass(String superClass) {
        return target -> target.hasSuperclass(superClass);
    }

    public SerializablePredicate<ClassInfo> innerClassesOf(String parent) {
        var parentFullName = parent + "$";
        return target -> target.name().startsWith(parentFullName);
    }

    public SerializablePredicate<MethodInfo> methodsReturning(String type) {
        var endMatch = ")L" + type + ";";
        return methodInfo -> methodInfo.descriptor().endsWith(endMatch);
    }

    public SerializablePredicate<FieldInfo> fieldsOfType(SerializablePredicate<ClassInfo> type) {
        return value -> type.test(value.type());
    }

    public <T> SerializablePredicate<T> matchAny() {
        return value -> true;
    }

    public <T extends Named> SerializablePredicate<T> not(SerializablePredicate<T> pred) {
        return target -> !pred.test(target);
    }

    public record AtGroup(String name, Modifier modifier, SerializablePredicate<ClassInfo> classMatch,
                          @Nullable SerializablePredicate<MethodInfo> methodMatch, @Nullable SerializablePredicate<FieldInfo> fieldMatch) implements Serializable {
    }

    public record Modifier(String name, boolean isFinal, int... validOpcodes) implements Serializable {
        public boolean test(int value) {
            if (isFinal && (value & Opcodes.ACC_FINAL) == 0) return false;

            for (int validOpcode : validOpcodes) {
                if ((value & validOpcode) != 0) {
                    return true;
                }
            }
            return false;
        }
    }
}
