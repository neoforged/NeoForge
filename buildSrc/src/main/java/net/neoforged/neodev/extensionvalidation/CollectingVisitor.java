package net.neoforged.neodev.extensionvalidation;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class CollectingVisitor extends ClassVisitor {
    private static final String EXTENSION_METHOD_ANNO = "Lnet/neoforged/neoforge/internal/ExtensionMethod;";
    static final String METHOD_DESC_ANNO = "Lnet/neoforged/neoforge/internal/MethodDesc;";

    private final Map<String, String> itfInjectTargets;
    final List<CheckExtensions.ExtensionDefinition> definitions = new ArrayList<>();
    private String currentClass;

    CollectingVisitor(Map<String, String> itfInjectTargets) {
        super(Opcodes.ASM9);
        this.itfInjectTargets = itfInjectTargets;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        currentClass = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(api) {
            private ExtensionPrototype prototype;

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (!descriptor.equals(EXTENSION_METHOD_ANNO)) {
                    return null;
                }

                prototype = new ExtensionPrototype();
                return new AnnotationVisitor(api) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String fieldName, String descriptor) {
                        if ("original".equals(fieldName) && descriptor.equals(METHOD_DESC_ANNO)) {
                            return new MethodDescAnnotationVisitor(api, name, desc -> prototype.original = desc);
                        }
                        return super.visitAnnotation(fieldName, descriptor);
                    }

                    @Override
                    public AnnotationVisitor visitArray(String fieldName) {
                        if (fieldName.equals("exclusions")) {
                            return new MethodDescAnnotationVisitor.ArrayVisitor(api, name, prototype.exclusions::add);
                        }
                        return null;
                    }
                };
            }

            @Override
            public void visitEnd() {
                if (prototype != null) {
                    if (prototype.original == null) {
                        String injectTarget = itfInjectTargets.get(currentClass);
                        if (injectTarget == null) {
                            throw new IllegalArgumentException("Class " + currentClass + " is not an injected interface, cannot automatically determine original method owner");
                        }
                        prototype.original = new CheckExtensions.MethodDesc(injectTarget, name, null);
                    }
                    definitions.add(new CheckExtensions.ExtensionDefinition(
                            prototype.original,
                            new CheckExtensions.MethodDesc(currentClass, name, descriptor),
                            prototype.exclusions
                    ));
                }
            }
        };
    }

    private static final class ExtensionPrototype {
        private CheckExtensions.MethodDesc original = null;
        private final Set<CheckExtensions.MethodDesc> exclusions = new HashSet<>();
    }
}
