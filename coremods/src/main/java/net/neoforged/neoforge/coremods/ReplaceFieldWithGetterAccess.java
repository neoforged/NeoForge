/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.coremods;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
public class ReplaceFieldWithGetterAccess implements ITransformer<ClassNode> {
    private final Map<String, String> fieldToMethod;
    private final Set<Target<ClassNode>> targets;

    public ReplaceFieldWithGetterAccess(String className, Map<String, String> fieldToMethod) {
        this.targets = Set.of(Target.targetClass(className));
        this.fieldToMethod = fieldToMethod;
    }

    @Override
    public TargetType<ClassNode> getTargetType() {
        return TargetType.CLASS;
    }

    @Override
    public Set<Target<ClassNode>> targets() {
        return targets;
    }

    @Override
    public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        for (var entry : fieldToMethod.entrySet()) {
            redirectFieldToMethod(input, entry.getKey(), entry.getValue());
        }

        return input;
    }

    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
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
