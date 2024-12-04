/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.coremods;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces code such as {@code itemstack.getItem() == Items.CROSSBOW} with instanceof checks such
 * as {@code itemstack.getItem() instanceof CrossbowItem}.
 * This transformer targets a set of methods to replace the occurrence of a single field-comparison.
 */
public class ReplaceFieldComparisonWithInstanceOf implements ITransformer<MethodNode> {
    private static final Logger LOG = LoggerFactory.getLogger(ReplaceFieldComparisonWithInstanceOf.class);

    private final Set<Target<MethodNode>> targets;
    private final String fieldOwner;
    private final String fieldName;
    private final String replacementClassName;

    /**
     * @param fieldOwner           The class that owns {@code fieldName}
     * @param fieldName            The name of a field in {@code fieldOwner}
     * @param replacementClassName Reference comparisons against {@code fieldName} in {@code fieldOwner} are replaced
     *                             by instanceof checks against this class.
     * @param methodsToScan        The methods to scan
     */
    public ReplaceFieldComparisonWithInstanceOf(String fieldOwner,
            String fieldName,
            String replacementClassName,
            List<Target<MethodNode>> methodsToScan) {
        this.targets = Set.copyOf(methodsToScan);

        this.fieldOwner = fieldOwner;
        this.fieldName = fieldName;
        this.replacementClassName = replacementClassName;
    }

    @Override
    public TargetType<MethodNode> getTargetType() {
        return TargetType.METHOD;
    }

    @Override
    public Set<Target<MethodNode>> targets() {
        return targets;
    }

    @Override
    public MethodNode transform(MethodNode methodNode, ITransformerVotingContext votingContext) {
        var count = 0;
        for (var node = methodNode.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof JumpInsnNode jumpNode && (jumpNode.getOpcode() == Opcodes.IF_ACMPEQ || jumpNode.getOpcode() == Opcodes.IF_ACMPNE)) {
                if (node.getPrevious() instanceof FieldInsnNode fieldAccessNode && (fieldAccessNode.getOpcode() == Opcodes.GETSTATIC || fieldAccessNode.getOpcode() == Opcodes.GETFIELD)) {
                    if (fieldAccessNode.owner.equals(fieldOwner) && fieldAccessNode.name.equals(fieldName)) {
                        methodNode.instructions.set(fieldAccessNode, new TypeInsnNode(Opcodes.INSTANCEOF, replacementClassName));
                        methodNode.instructions.set(jumpNode, new JumpInsnNode(jumpNode.getOpcode() == Opcodes.IF_ACMPEQ ? Opcodes.IFNE : Opcodes.IFEQ, jumpNode.label));
                        count++;
                    }
                }
            }
        }

        LOG.trace("Transforming: {}.", methodNode.name);
        LOG.trace("field_to_instance: Replaced {} checks", count);

        return methodNode;
    }

    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }
}
