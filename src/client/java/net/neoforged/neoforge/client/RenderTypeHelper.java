/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides helper functions replacing those in {@link ItemBlockRenderTypes}.
 */
@SuppressWarnings("deprecation")
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
        return Sheets.translucentBlockItemSheet();
    }

    /**
     * Provides a {@link RenderType} fit for rendering moving blocks given the specified chunk render type.
     * This should be called for each {@link RenderType} returned by {@link BlockModelPart#getRenderType(BlockState)}.
     * <p>
     * Mimics the behavior of vanilla's {@link ItemBlockRenderTypes#getMovingBlockRenderType(BlockState)}.
     */
    public static RenderType getMovingBlockRenderType(ChunkSectionLayer chunkSectionLayer) {
        return switch (chunkSectionLayer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
            case TRIPWIRE -> RenderTypes.tripwireMovingBlock();
        };
    }

    /**
     * Determines whether the provided quads need use textures from the block atlas or the item atlas and returns the
     * respective {@link RenderType} getter.
     * <p>
     * Mimics the behavior of vanilla's {@link BlockModelWrapper#detectRenderType(List)}.
     */
    public static Function<ItemStack, RenderType> detectItemModelRenderType(List<BakedQuad> quads, RenderTypeGroup renderTypes) {
        Iterator<BakedQuad> iterator = quads.iterator();
        if (!iterator.hasNext()) {
            return $ -> renderTypes.entityItem();
        }

        Identifier atlasId = iterator.next().sprite().atlasLocation();
        while (iterator.hasNext()) {
            Identifier currAtlasId = iterator.next().sprite().atlasLocation();
            if (!currAtlasId.equals(atlasId)) {
                throw new IllegalStateException("Multiple atlases used in model, expected " + atlasId + ", but also got " + currAtlasId);
            }
        }

        if (atlasId.equals(TextureAtlas.LOCATION_ITEMS)) {
            return $ -> renderTypes.entityItem();
        }
        if (atlasId.equals(TextureAtlas.LOCATION_BLOCKS)) {
            return $ -> renderTypes.entityBlock();
        }
        throw new IllegalArgumentException("Atlas " + atlasId + " can't be used for item models");
    }

    private RenderTypeHelper() {}
}
