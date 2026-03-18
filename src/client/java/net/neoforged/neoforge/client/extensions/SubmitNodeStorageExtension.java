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

    record MultiLayerBlockModelSubmit(
            PoseStack.Pose pose,
            List<BlockStateModelPart> modelParts,
            boolean translucent,
            int[] tintLayers,
            int lightCoords,
            int overlayCoords,
            int outlineColor) {}
}
