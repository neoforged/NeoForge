package net.neoforged.neodev;

import org.apache.commons.lang3.mutable.MutableInt;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.zip.ZipFile;

public abstract class GenerateAccessTransformers extends DefaultTask {
    public static final Modifier PUBLIC = mod("public", false, Opcodes.ACC_PUBLIC);
    public static final Modifier PROTECTED = mod("protected", false, Opcodes.ACC_PUBLIC, Opcodes.ACC_PROTECTED);

    @InputFile
    public abstract RegularFileProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getAccessTransformer();

    @Input
    public abstract ListProperty<AtGroup> getGroups();

    @TaskAction
    public void exec() throws IOException {
        Map<String, ClassInfo> targets = new HashMap<>();
        var visitor = visitor(targets);

        try (var zip = new ZipFile(getInput().getAsFile().get())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var next = entries.nextElement();
                if (next.isDirectory() || !next.getName().endsWith(".class")) continue;

                var lastInner = next.getName().lastIndexOf("$");
                if (lastInner >= 0) {
                    // Skip anonymous classes
                    if (Character.isDigit(next.getName().charAt(lastInner + 1))) continue;
                }

                try (var in = zip.getInputStream(next)) {
                    var reader = new ClassReader(in);
                    reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
            }
        }

        var groupList = getGroups().get();
        List<String>[] groups = IntStream.range(0, groupList.size())
                .mapToObj(i -> new ArrayList<String>()).toArray(List[]::new);

        for (ClassInfo value : targets.values()) {
            for (int i = 0; i < groupList.size(); i++) {
                var group = groupList.get(i);
                if (group.classMatch.test(value)) {
                    if (group.fieldMatch != null) {
                        for (var field : value.fields) {
                            if (group.fieldMatch.test(field) && !group.modifier.test(field.access)) {
                                groups[i].add(group.modifier + " " + value.name.replace('/', '.') + " " + field.name);
                            }
                        }
                    } else if (group.methodMatch != null) {
                        for (var method : value.methods) {
                            if (group.methodMatch.test(method) && !group.modifier.test(method.access)) {
                                groups[i].add(group.modifier + " " + value.name.replace('/', '.') + " " + method.name + method.descriptor);
                            }
                        }
                    } else if (!group.modifier.test(value.access.intValue())) {
                        groups[i].add(group.modifier + " " + value.name.replace('/', '.'));
                    }
                }
            }
        }

        var text = new StringBuilder();
        for (int i = 0; i < groups.length; i++) {
            text.append("#group ").append(groupList.get(i).name).append('\n')
                    .append(String.join("\n", groups[i])).append("\n#endgroup\n");
        }

        var outFile = getAccessTransformer().getAsFile().get().toPath();
        if (!Files.exists(outFile.getParent())) {
            Files.createDirectories(outFile.getParent());
        }

        Files.writeString(outFile, text);
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

    public SerializablePredicate<ClassInfo> matchClass(String name) {
        return target -> target.name.equals(name);
    }

    public SerializablePredicate<ClassInfo> matchClassesWithSuperclass(String superClass) {
        return target -> target.hasSuperclass(superClass);
    }

    public SerializablePredicate<ClassInfo> matchInnerClassesOf(String parent) {
        var parentFullName = parent + "$";
        return target -> target.name.startsWith(parentFullName);
    }

    public SerializablePredicate<MethodInfo> matchMethodsWithName(String name) {
        return methodInfo -> methodInfo.name.equals(name);
    }

    public SerializablePredicate<MethodInfo> matchMethodsReturning(String type) {
        var endMatch = ")L" + type + ";";
        return methodInfo -> methodInfo.descriptor.endsWith(endMatch);
    }

    public SerializablePredicate<FieldInfo> matchFieldsOfType(SerializablePredicate<ClassInfo> type) {
        return value -> type.test(value.type);
    }

    public <T> SerializablePredicate<T> matchAny() {
        return value -> true;
    }

    private ClassVisitor visitor(Map<String, ClassInfo> targets) {
        return new ClassVisitor(Opcodes.ASM9) {
            ClassInfo current;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                current = GenerateAccessTransformers.getClass(targets, name);
                current.access.setValue(access);
                if (superName != null) {
                    current.parents.add(GenerateAccessTransformers.getClass(targets, superName));
                }
                for (String iface : interfaces) {
                    current.parents.add(GenerateAccessTransformers.getClass(targets, iface));
                }
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                if (name.equals(current.name)) {
                    current.access.setValue(access);
                }
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                current.fields.add(new FieldInfo(name, GenerateAccessTransformers.getClass(targets, Type.getType(descriptor).getInternalName()), access));
                return null;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                current.addMethod(name, descriptor, access);
                return null;
            }
        };
    }

    private static ClassInfo getClass(Map<String, ClassInfo> targets, String name) {
        var existing = targets.get(name);
        if (existing != null) return existing;
        existing = new ClassInfo(name, new MutableInt(0), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        targets.put(name, existing);
        return existing;
    }

    public record ClassInfo(String name, MutableInt access, List<ClassInfo> parents, List<MethodInfo> methods, List<FieldInfo> fields) {
        public void addMethod(String name, String desc, int access) {
            this.methods.add(new MethodInfo(name, desc, access));
        }

        public boolean hasSuperclass(String name) {
            for (ClassInfo parent : parents) {
                if (parent.hasSuperclass(name)) {
                    return true;
                }
            }
            return this.name.equals(name);
        }
    }
    public record MethodInfo(String name, String descriptor, int access) {}
    public record FieldInfo(String name, ClassInfo type, int access) {}

    public record AtGroup(String name, Modifier modifier, SerializablePredicate<ClassInfo> classMatch,
                          @Nullable SerializablePredicate<MethodInfo> methodMatch, @Nullable SerializablePredicate<FieldInfo> fieldMatch) implements Serializable {
    }

    @FunctionalInterface
    public interface SerializablePredicate<T> extends Serializable {
        boolean test(T value);
    }

    @FunctionalInterface
    public interface Modifier extends Serializable {
        boolean test(int value);
    }

    private static Modifier mod(String name, boolean isFinal, int... validOpcodes) {
        return new Modifier() {
            @Override
            public boolean test(int value) {
                if (isFinal && (value & Opcodes.ACC_FINAL) == 0) return false;

                for (int validOpcode : validOpcodes) {
                    if ((value & validOpcode) != 0) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}
