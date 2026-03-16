/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import java.util.Map;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;

public class CompositeUnbakedGeometry implements ExtendedUnbakedGeometry {
    final ImmutableMap<String, Either<Identifier, UnbakedModel>> children;

    public CompositeUnbakedGeometry(ImmutableMap<String, Either<Identifier, UnbakedModel>> children) {
        this.children = children;
    }

    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
        Transformation rootTransform = additionalProperties.getOrDefault(NeoForgeModelProperties.TRANSFORM, Transformation.IDENTITY);
        if (!rootTransform.isIdentity())
            state = UnbakedElementsHelper.composeRootTransformIntoModelState(state, rootTransform);

        Map<String, Boolean> partVisibility = additionalProperties.getOrDefault(NeoForgeModelProperties.PART_VISIBILITY, Map.of());

        QuadCollection.Builder quadBuilder = new QuadCollection.Builder();

        for (var entry : children.entrySet()) {
            var name = entry.getKey();
            if (!partVisibility.getOrDefault(name, true))
                continue;
            ResolvedModel model = entry.getValue().map(
                    baker::getModel,
                    inline -> baker.resolveInlineModel(inline, () -> debugName.debugName() + "_" + entry.getKey()));
            var modelGeometry = model.bakeTopGeometry(model.getTopTextureSlots(), baker, state);
            quadBuilder.addAll(modelGeometry);
        }

        return quadBuilder.build();
    }
}
