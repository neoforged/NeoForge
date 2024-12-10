/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.context.ContextMap;

/**
 * Base interface for unbaked models that wish to support the NeoForge-added {@code bake} method
 * that receives {@linkplain #fillAdditionalProperties(ContextMap.Builder) additional properties}.
 */
public interface ExtendedUnbakedModel extends UnbakedModel {
    @Deprecated
    @Override
    default BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms) {
        return bake(textures, baker, modelState, useAmbientOcclusion, usesBlockLight, itemTransforms, ContextMap.EMPTY);
    }

    // Re-abstract the extended version
    @Override
    BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties);
}
