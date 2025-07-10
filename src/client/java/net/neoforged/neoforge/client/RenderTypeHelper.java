/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides helper functions replacing those in {@link ItemBlockRenderTypes}.
 */
public final class RenderTypeHelper {
    /**
     * Provides a {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY} for the given {@link DefaultVertexFormat#BLOCK} format.
     * This should be called for each {@link RenderType} returned by {@link BlockModelPart#getRenderType(BlockState)}.
     * <p>
     * Mimics the behavior of vanilla's {@link ItemBlockRenderTypes#getRenderType(BlockState)}.
     */
    public static RenderType getEntityRenderType(ChunkSectionLayer chunkSectionLayer) {
        if (chunkSectionLayer != ChunkSectionLayer.TRANSLUCENT)
            return Sheets.cutoutBlockSheet();
        return Sheets.translucentItemSheet();
    }

    /**
     * Provides a {@link RenderType} fit for rendering moving blocks given the specified chunk render type.
     * This should be called for each {@link RenderType} returned by {@link BlockModelPart#getRenderType(BlockState)}.
     * <p>
     * Mimics the behavior of vanilla's {@link ItemBlockRenderTypes#getMovingBlockRenderType(BlockState)}.
     */
    public static RenderType getMovingBlockRenderType(ChunkSectionLayer chunkSectionLayer) {
        return switch (chunkSectionLayer) {
            case SOLID -> RenderType.solid();
            case CUTOUT_MIPPED -> RenderType.cutoutMipped();
            case CUTOUT -> RenderType.cutout();
            case TRANSLUCENT -> RenderType.translucentMovingBlock();
            case TRIPWIRE -> RenderType.tripwire();
        };
    }

    private RenderTypeHelper() {}
}
