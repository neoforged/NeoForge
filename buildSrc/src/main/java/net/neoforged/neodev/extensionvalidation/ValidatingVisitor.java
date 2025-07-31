package net.neoforged.neodev.extensionvalidation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class ValidatingVisitor extends ClassVisitor {
    // Class name -> set of method descriptors
    private final Map<String, Set<String>> expectedOriginals;
    // Class name -> set of method descriptors
    private final Map<String, Set<String>> expectedReplacements;
    private final Set<String> originalMethods;
    // Class name -> set of method descriptors
    final Map<String, Set<String>> locatedOriginals = new HashMap<>();
    // Class name -> set of method descriptors
    final Map<String, Set<String>> locatedReplacements = new HashMap<>();
    // Calling method descriptor -> callee method descriptors
    final Map<CheckExtensions.MethodDesc, Set<CheckExtensions.MethodDesc>> unreplacedTargets = new HashMap<>();
    // Inherited class -> set of inheriting classes
    final Map<String, Set<String>> inheritors = new HashMap<>();
    private String currentClass;

    ValidatingVisitor(Map<String, Set<String>> expectedOriginals, Map<String, Set<String>> expectedReplacements) {
        super(Opcodes.ASM9);
        this.expectedOriginals = expectedOriginals;
        this.expectedReplacements = expectedReplacements;
        this.originalMethods = expectedOriginals.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        currentClass = name;
        if (!superName.equals("java/lang/Object")) {
            inheritors.computeIfAbsent(superName, $ -> new HashSet<>()).add(name);
        }
        for (String itf : interfaces) {
            inheritors.computeIfAbsent(itf, $ -> new HashSet<>()).add(name);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        String methodDesc = name + descriptor;
        Set<String> expectedOriginalMethods = expectedOriginals.get(currentClass);
        if (expectedOriginalMethods != null && expectedOriginalMethods.contains(methodDesc)) {
            locatedOriginals.computeIfAbsent(currentClass, $ -> new HashSet<>()).add(methodDesc);
        }
        Set<String> expectedReplacementMethods = expectedReplacements.get(currentClass);
        if (expectedReplacementMethods != null && expectedReplacementMethods.contains(methodDesc)) {
            locatedReplacements.computeIfAbsent(currentClass, $ -> new HashSet<>()).add(methodDesc);
        }

        CheckExtensions.MethodDesc outerDesc = new CheckExtensions.MethodDesc(currentClass, name, descriptor);
        return new MethodVisitor(api) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String calleeName, String calleeDescriptor, boolean isInterface) {
                if (opcode == Opcodes.INVOKESPECIAL && calleeName.equals(name) && calleeDescriptor.equals(descriptor)) {
                    // Ignore super calls
                    return;
                }

                if (originalMethods.contains(calleeName + calleeDescriptor)) {
                    unreplacedTargets.computeIfAbsent(outerDesc, $ -> new HashSet<>())
                            .add(new CheckExtensions.MethodDesc(owner, calleeName, calleeDescriptor));
                }
            }
        };
    }
}
