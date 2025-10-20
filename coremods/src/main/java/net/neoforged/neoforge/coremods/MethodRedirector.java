/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.coremods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Redirect calls to one method to another.
 */
public class MethodRedirector extends SimpleClassProcessor {
    private final Map<String, List<MethodRedirection>> redirectionsByClass = new HashMap<>();
    private final Set<Target> targets = new HashSet<>();

    private static final List<MethodRedirection> REDIRECTIONS = List.of(
            new MethodRedirection(
                    Opcodes.INVOKEVIRTUAL,
                    "finalizeSpawn",
                    "(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;",
                    "finalize_spawn_targets.json",
                    methodInsnNode -> new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "net/neoforged/neoforge/event/EventHooks",
                            "finalizeMobSpawn",
                            "(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;",
                            false)));

    public MethodRedirector() {
        for (var redirection : REDIRECTIONS) {
            var targetClassNames = CoremodUtils.loadResource(redirection.targetClassListFile, String[].class);
            for (var targetClassName : targetClassNames) {
                targets.add(new Target(targetClassName));
                var redirections = redirectionsByClass.computeIfAbsent(targetClassName, s -> new ArrayList<>());
                redirections.add(redirection);
            }
        }
    }

    @Override
    public ProcessorName name() {
        return new ProcessorName("neoforge.coremods", "method_redirector");
    }

    @Override
    public Set<Target> targets() {
        return targets;
    }

    @Override
    public void transform(ClassNode classNode, SimpleTransformationContext context) {
        var redirections = redirectionsByClass.getOrDefault(context.type().getClassName(), Collections.emptyList());

        var methods = classNode.methods;
        for (var method : methods) {
            var instr = method.instructions;
            for (var i = 0; i < instr.size(); i++) {
                var node = instr.get(i);
                if (node instanceof MethodInsnNode methodInsnNode) {
                    for (var redirection : redirections) {
                        if (redirection.invokeOpCode == methodInsnNode.getOpcode()
                                && redirection.methodName.equals(methodInsnNode.name)
                                && redirection.methodDescriptor.equals(methodInsnNode.desc)) {
                            // Found a match for the target method
                            instr.set(
                                    methodInsnNode,
                                    redirection.redirector.apply(methodInsnNode));
                        }
                    }
                }
            }
        }
    }

    private record MethodRedirection(
            int invokeOpCode,
            String methodName,
            String methodDescriptor,
            String targetClassListFile,
            Function<MethodInsnNode, MethodInsnNode> redirector) {}
}
