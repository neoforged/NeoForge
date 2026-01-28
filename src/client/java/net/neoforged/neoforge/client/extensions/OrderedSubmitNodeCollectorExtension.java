/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.state.BlockState;

public interface OrderedSubmitNodeCollectorExtension {
    private OrderedSubmitNodeCollector self() {
        return (OrderedSubmitNodeCollector) this;
    }

    /**
     * Extended version of {@link OrderedSubmitNodeCollector#submitBlockModel(PoseStack, RenderType, BlockStateModel, float, float, float, int, int, int)} with
     * support for per-{@link BlockModelPart} {@link ChunkSectionLayer} lookup based on the provided {@link BlockState}.
     *
     * @param poseStack     The {@code PoseStack} to render the model with
     * @param state         The {@code BlockState} to use for per-part {@code ChunkSectionLayer} lookup
     * @param model         The model to render
     * @param r             The red channel color multiplier
     * @param g             The green channel color multiplier
     * @param b             The blue channel color multiplier
     * @param lightCoords   The packed light value to render the model with
     * @param overlayCoords The overlay coordinates to render the model with
     * @param outlineColor  The outline color to render the model with or {@code 0} to not render an outline
     *                      the provided {@code RenderType} to be used
     */
    default void submitBlockModel(
            PoseStack poseStack,
            BlockState state,
            BlockStateModel model,
            float r,
            float g,
            float b,
            int lightCoords,
            int overlayCoords,
            int outlineColor) {
        self().submitBlockModel(poseStack, ItemBlockRenderTypes.getRenderType(state), model, r, g, b, lightCoords, overlayCoords, outlineColor);
    }
}
