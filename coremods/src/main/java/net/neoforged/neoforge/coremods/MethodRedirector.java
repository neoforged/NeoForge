/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.coremods;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Redirect calls to one method to another.
 */
public class MethodRedirector implements ITransformer<ClassNode> {
    private final Map<String, List<MethodRedirection>> redirectionsByClass = new HashMap<>();
    private final Set<Target<ClassNode>> targets = new HashSet<>();

    private static final List<MethodRedirection> REDIRECTIONS = List.of(
            new MethodRedirection(
                    Opcodes.INVOKEVIRTUAL,
                    "finalizeSpawn",
                    "(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;",
                    "finalize_spawn_targets.json",
                    methodInsnNode -> new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "net/neoforged/neoforge/event/EventHooks",
                            "finalizeMobSpawn",
                            "(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;",
                            false)));

    public MethodRedirector() {
        for (var redirection : REDIRECTIONS) {
            var targetClassNames = CoremodUtils.loadResource(redirection.targetClassListFile, String[].class);
            for (var targetClassName : targetClassNames) {
                targets.add(Target.targetClass(targetClassName));
                var redirections = redirectionsByClass.computeIfAbsent(targetClassName, s -> new ArrayList<>());
                redirections.add(redirection);
            }
        }
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
    public ClassNode transform(ClassNode classNode, ITransformerVotingContext votingContext) {
        var redirections = redirectionsByClass.getOrDefault(classNode.name, Collections.emptyList());

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
        return classNode;
    }

    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    private record MethodRedirection(
            int invokeOpCode,
            String methodName,
            String methodDescriptor,
            String targetClassListFile,
            Function<MethodInsnNode, MethodInsnNode> redirector) {}
}
