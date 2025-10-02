/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.coremods;

import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Replaces direct field access in a class with access to the getter.
 * <p>
 * The field specified by fieldName must be private and non-static.
 * The method-call the field-access is redirected to does not take any parameters and returns an object of the
 * same type as the field.
 * If no methodName is passed, any method matching the described signature will be used as callable method.
 */
public class ReplaceFieldWithGetterAccess extends SimpleClassProcessor {
    private final Map<String, String> fieldToMethod;
    private final Set<Target> targets;
    private final String className;

    public ReplaceFieldWithGetterAccess(String className, Map<String, String> fieldToMethod) {
        this.targets = Set.of(new Target(className));
        this.fieldToMethod = fieldToMethod;
        this.className = className;
    }

    @Override
    public ProcessorName name() {
        var owner = className.toLowerCase(Locale.ROOT).replace('$', '.');
        return new ProcessorName("neoforge.coremods", "field_to_getter." + owner);
    }

    @Override
    public Set<Target> targets() {
        return targets;
    }

    @Override
    public void transform(ClassNode input, SimpleTransformationContext context) {
        for (var entry : fieldToMethod.entrySet()) {
            redirectFieldToMethod(input, entry.getKey(), entry.getValue());
        }
    }

    private static void redirectFieldToMethod(ClassNode classNode, String fieldName, @Nullable String methodName) {
        var foundField = CoremodUtils.getFieldByName(classNode, fieldName);

        if (!Modifier.isPrivate(foundField.access) || Modifier.isStatic(foundField.access)) {
            throw new IllegalStateException("Field " + fieldName + " in " + classNode.name + " is not private and an instance field");
        }

        String methodDescriptor = "()" + foundField.desc;

        var foundMethod = CoremodUtils.getMethodByDescriptor(classNode, methodName, methodDescriptor);

        for (var methodNode : classNode.methods) {
            // skip the found getter method
            if (methodNode != foundMethod && !Objects.equals(methodNode.desc, methodDescriptor)) {
                var iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    var insnNode = iterator.next();
                    if (insnNode.getOpcode() == Opcodes.GETFIELD) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
                        if (Objects.equals(fieldInsnNode.name, fieldName)) {
                            iterator.remove();
                            MethodInsnNode replace = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classNode.name, foundMethod.name, foundMethod.desc, false);
                            iterator.add(replace);
                        }
                    }
                }
            }
        }
    }
}
