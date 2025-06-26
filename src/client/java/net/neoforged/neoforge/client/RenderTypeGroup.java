/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

/**
 * A set of functionally equivalent shaders. One using {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#BLOCK},
 * and the other one using {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#NEW_ENTITY}.
 */
public record RenderTypeGroup(ChunkSectionLayer block, RenderType entity) {
    public RenderTypeGroup {
        if ((block == null) != (entity == null))
            throw new IllegalArgumentException("The render types in a group must either be all null, or all non-null.");
    }

    public static RenderTypeGroup EMPTY = new RenderTypeGroup(null, null);

    /**
     * {@return true if this group has render types or not. It either has all, or none}
     */
    public boolean isEmpty() {
        // We throw an exception in the constructor if nullability doesn't match, so checking this is enough
        return block == null;
    }
}
