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
import org.jetbrains.annotations.Nullable;

public abstract class DelegateUnbakedModel implements ExtendedUnbakedModel {
    protected final UnbakedModel wrapped;

    protected DelegateUnbakedModel(UnbakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        return this.wrapped.bake(textures, baker, modelState, useAmbientOcclusion, usesBlockLight, itemTransforms, additionalProperties);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        this.wrapped.resolveDependencies(resolver);
    }

    @Nullable
    @Override
    public Boolean getAmbientOcclusion() {
        return this.wrapped.getAmbientOcclusion();
    }

    @Nullable
    @Override
    public GuiLight getGuiLight() {
        return this.wrapped.getGuiLight();
    }

    @Nullable
    @Override
    public ItemTransforms getTransforms() {
        return this.wrapped.getTransforms();
    }

    @Override
    public TextureSlots.Data getTextureSlots() {
        return this.wrapped.getTextureSlots();
    }

    @Nullable
    @Override
    public UnbakedModel getParent() {
        return this.wrapped.getParent();
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        this.wrapped.fillAdditionalProperties(propertiesBuilder);
    }
}
