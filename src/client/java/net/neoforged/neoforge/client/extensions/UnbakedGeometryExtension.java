/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.util.context.ContextMap;

public interface UnbakedGeometryExtension {
    private UnbakedGeometry self() {
        return (UnbakedGeometry) this;
    }

    /**
     * Version of {@link UnbakedGeometry#bake(TextureSlots, ModelBaker, ModelState, ModelDebugName)}
     * that also receives additional properties sourced from {@link UnbakedModelExtension#fillAdditionalProperties}.
     */
    default QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
        return self().bake(textureSlots, baker, state, debugName);
    }
}
