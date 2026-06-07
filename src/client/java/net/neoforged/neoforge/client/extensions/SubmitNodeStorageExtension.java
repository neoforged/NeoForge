/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.neoforged.neoforge.client.submit.RenderPhaseKey;

public interface SubmitNodeStorageExtension extends SubmitNodeCollector {
    private SubmitNodeStorage self() {
        return (SubmitNodeStorage) this;
    }

    @Override
    default void submitMultiLayerBlockModel(
            PoseStack poseStack,
            List<BlockStateModelPart> modelParts,
            boolean translucent,
            int[] tintLayers,
            int lightCoords,
            int overlayCoords,
            int outlineColor) {
        self().order(0).submitMultiLayerBlockModel(poseStack, modelParts, translucent, tintLayers, lightCoords, overlayCoords, outlineColor);
    }

    @Override
    default <T extends SubmitNode, S extends T> void submitSpecial(RenderPhaseKey<T> phaseKey, S submitNode) {
        self().order(0).submitSpecial(phaseKey, submitNode);
    }
}
