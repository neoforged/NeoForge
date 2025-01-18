/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.RenderTypeGroup;

/**
 * @deprecated Extend {@link ExtendedUnbakedModel} directly instead, and use {@link SimpleBakedModel.Builder} if appropriate.
 */
@Deprecated(forRemoval = true, since = "1.21.4")
public abstract class AbstractSimpleUnbakedModel implements ExtendedUnbakedModel {
    @Override
    public BakedModel bake(TextureSlots slots,
            ModelBaker baker,
            ModelState state,
            boolean useAmbientOcclusion,
            boolean usesBlockLight,
            ItemTransforms transforms,
            ContextMap additionalProperties) {
        TextureAtlasSprite particle = baker.findSprite(slots, TextureSlot.PARTICLE.getId());
        var renderTypes = additionalProperties.getOrDefault(NeoForgeModelProperties.RENDER_TYPE, RenderTypeGroup.EMPTY);

        IModelBuilder<?> builder = IModelBuilder.of(useAmbientOcclusion, usesBlockLight, isGui3d(),
                transforms, particle, renderTypes);

        addQuads(builder, slots, baker, state, useAmbientOcclusion, usesBlockLight, transforms);

        return builder.build();
    }

    @Override
    public void resolveDependencies(Resolver p_387087_) {
        //Has no dependencies
    }

    public abstract void addQuads(
            IModelBuilder<?> builder,
            TextureSlots slots,
            ModelBaker baker,
            ModelState state,
            boolean useAmbientOcclusion,
            boolean usesBlockLight,
            ItemTransforms transforms);

    protected boolean isGui3d() {
        return true;
    }
}
