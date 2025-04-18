/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

/**
 * Provides helper functions replacing those in {@link ItemBlockRenderTypes}.
 */
public final class RenderTypeHelper {
    /**
     * Provides a {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY} for the given {@link DefaultVertexFormat#BLOCK} format.
     * This should be called for each {@link RenderType} returned by {@link BlockStateModel#getRenderTypes(BlockState, RandomSource, ModelData)}.
     * <p>
     * Mimics the behavior of vanilla's {@link ItemBlockRenderTypes#getRenderType(BlockState)}.
     */
    public static RenderType getEntityRenderType(RenderType chunkRenderType) {
        if (chunkRenderType != RenderType.translucent())
            return Sheets.cutoutBlockSheet();
        return Sheets.translucentItemSheet();
    }

    /**
     * Provides a {@link RenderType} fit for rendering moving blocks given the specified chunk render type.
     * This should be called for each {@link RenderType} returned by {@link BlockStateModel#getRenderTypes(BlockState, RandomSource, ModelData)}.
     * <p>
     * Mimics the behavior of vanilla's {@link ItemBlockRenderTypes#getMovingBlockRenderType(BlockState)}.
     */
    public static RenderType getMovingBlockRenderType(RenderType renderType) {
        if (renderType == RenderType.translucent())
            return RenderType.translucentMovingBlock();
        return renderType;
    }

    private RenderTypeHelper() {}
}
