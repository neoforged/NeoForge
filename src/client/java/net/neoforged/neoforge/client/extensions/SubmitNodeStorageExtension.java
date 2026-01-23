/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface SubmitNodeStorageExtension extends OrderedSubmitNodeCollectorExtension {
    private SubmitNodeStorage self() {
        return (SubmitNodeStorage) this;
    }

    @Override
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
        self().order(0).submitBlockModel(poseStack, renderType, model, r, g, b, lightCoords, overlayCoords, outlineColor, state);
    }
}
