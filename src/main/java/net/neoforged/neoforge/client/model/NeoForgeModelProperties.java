/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.RenderTypeGroup;

/**
 * Properties that NeoForge adds for {@link BlockModel}s and {@link UnbakedModel}s.
 */
public final class NeoForgeModelProperties {
    private NeoForgeModelProperties() {}

    /**
     * Root transform. For block models, this can be specified under the {@code transform} JSON key.
     */
    public static final ContextKey<Transformation> TRANSFORM = ContextKey.vanilla("transform");

    /**
     * Render type to use. For block models, this can be specified under the {@code render_type} JSON key.
     */
    public static final ContextKey<RenderTypeGroup> RENDER_TYPE = ContextKey.vanilla("render_type");
}
