/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.extensions.UnbakedModelExtension;

/**
 * Base interface for unbaked models that wish to support the NeoForge-added {@code bake} method
 * that receives {@linkplain UnbakedModelExtension#fillAdditionalProperties(ContextMap.Builder) additional properties}.
 */
@FunctionalInterface
public interface ExtendedUnbakedGeometry extends UnbakedGeometry {
    @Override
    default QuadCollection bake(TextureSlots p_405831_, ModelBaker p_405026_, ModelState p_405122_, ModelDebugName p_405635_) {
        return bake(p_405831_, p_405026_, p_405122_, p_405635_, ContextMap.EMPTY);
    }

    // Re-abstract the extended version
    @Override
    QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties);
}
