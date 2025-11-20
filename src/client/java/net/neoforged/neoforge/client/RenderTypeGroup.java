/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.function.Function;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

/**
 * A set of functionally equivalent shaders. One using {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#BLOCK},
 * and the other two using {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#NEW_ENTITY}.
 */
public record RenderTypeGroup(ChunkSectionLayer block, RenderType entityBlock, RenderType entityItem) {
    public RenderTypeGroup {
        if ((block == null) != (entityBlock == null) || (block == null) != (entityItem == null)) {
            throw new IllegalArgumentException("The render types in a group must either be all null, or all non-null.");
        }
        if (entityBlock != null && (entityBlock.format() != DefaultVertexFormat.NEW_ENTITY || entityItem.format() != DefaultVertexFormat.NEW_ENTITY)) {
            throw new IllegalArgumentException("The entity render types must use DefaultVertexFormat.NEW_ENTITY.");
        }
    }

    public RenderTypeGroup(ChunkSectionLayer block, Function<Identifier, RenderType> entity) {
        this(block, entity.apply(TextureAtlas.LOCATION_BLOCKS), entity.apply(TextureAtlas.LOCATION_ITEMS));
    }

    public static RenderTypeGroup EMPTY = new RenderTypeGroup(null, null, null);

    /**
     * {@return true if this group has render types or not. It either has all, or none}
     */
    public boolean isEmpty() {
        // We throw an exception in the constructor if nullability doesn't match, so checking this is enough
        return block == null;
    }
}
