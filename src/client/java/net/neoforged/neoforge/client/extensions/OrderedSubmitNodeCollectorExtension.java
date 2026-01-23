/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface OrderedSubmitNodeCollectorExtension {
    private OrderedSubmitNodeCollector self() {
        return (OrderedSubmitNodeCollector) this;
    }

    default void submitBlockModel(
            PoseStack poseStack,
            RenderType renderType,
            BlockStateModel model,
            float r,
            float g,
            float b,
            int lightCoords,
            int overlayCoords,
            int outlineColor,
            @Nullable BlockState state) {
        self().submitBlockModel(poseStack, renderType, model, r, g, b, lightCoords, overlayCoords, outlineColor);
    }
}
