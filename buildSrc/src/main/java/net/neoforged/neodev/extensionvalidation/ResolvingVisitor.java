package net.neoforged.neodev.extensionvalidation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class ResolvingVisitor extends ClassVisitor {
    // Class name -> method names
    private final Map<String, Set<String>> toResolve = new HashMap<>();
    // Class name -> method name -> descriptor
    private final Map<String, Map<String, Optional<String>>> resolved = new HashMap<>();
    private String currentClass;
    private final Map<String, Set<String>> currentResolved = new HashMap<>();

    ResolvingVisitor(List<CheckExtensions.ExtensionDefinition> definitions) {
        super(Opcodes.ASM9);
        for (CheckExtensions.ExtensionDefinition definition : definitions) {
            if (definition.original().descriptor() == null) {
                addDescriptor(definition.original());
            }
            for (CheckExtensions.MethodDesc exclusion : definition.exclusions()) {
                if (exclusion.descriptor() == null) {
                    addDescriptor(exclusion);
                }
            }
        }
    }

    private void addDescriptor(CheckExtensions.MethodDesc desc) {
        toResolve.computeIfAbsent(desc.owner(), $ -> new HashSet<>()).add(desc.name());
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        currentClass = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        Set<String> setToResolve = toResolve.get(currentClass);
        if (setToResolve != null && setToResolve.contains(name)) {
            currentResolved.computeIfAbsent(name, $ -> new HashSet<>()).add(descriptor);
        }
        return null;
    }

    @Override
    public void visitEnd() {
        if (!toResolve.containsKey(currentClass)) {
            return;
        }

        Map<String, Optional<String>> resolvedInClass = resolved.computeIfAbsent(currentClass, $ -> new HashMap<>());
        for (Map.Entry<String, Set<String>> entry : currentResolved.entrySet()) {
            if (entry.getValue().size() == 1) {
                resolvedInClass.put(entry.getKey(), Optional.of(entry.getValue().iterator().next()));
            } else if (entry.getValue().size() > 1) {
                resolvedInClass.put(entry.getKey(), Optional.empty());
            }
        }
        currentResolved.clear();
    }

    private CheckExtensions.MethodDesc resolve(CheckExtensions.MethodDesc desc) {
        if (desc.descriptor() != null) {
            return desc;
        }
        Map<String, Optional<String>> resolvedInClass = resolved.get(desc.owner());
        if (resolvedInClass == null) {
            throw new IllegalStateException("Class " + desc.owner() + " does not exist");
        }
        Optional<String> descriptor = resolvedInClass.get(desc.name());
        if (descriptor == null) {
            throw new IllegalStateException("Class " + desc.owner() + " does not contain a method named " + desc.name());
        } else if (descriptor.isEmpty()) {
            throw new IllegalStateException("Class " + desc.owner() + " contains multiple methods named " + desc.name());
        }
        return new CheckExtensions.MethodDesc(desc.owner(), desc.name(), descriptor.get());
    }

    CheckExtensions.ExtensionDefinition resolve(CheckExtensions.ExtensionDefinition definition) {
        if (definition.isIncomplete()) {
            Set<CheckExtensions.MethodDesc> exclusions = new HashSet<>();
            for (CheckExtensions.MethodDesc exclusion : definition.exclusions()) {
                exclusions.add(resolve(exclusion));
            }
            return new CheckExtensions.ExtensionDefinition(resolve(definition.original()), definition.replacement(), exclusions);
        }
        return definition;
    }
}
