/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import org.jetbrains.annotations.Nullable;

public abstract class DelegateUnbakedModel implements UnbakedModel {
    protected final UnbakedModel delegate;

    protected DelegateUnbakedModel(UnbakedModel delegate) {
        this.delegate = delegate;
    }

    @Nullable
    @Override
    public Boolean ambientOcclusion() {
        return this.delegate.ambientOcclusion();
    }

    @Nullable
    @Override
    public GuiLight guiLight() {
        return this.delegate.guiLight();
    }

    @Nullable
    @Override
    public ItemTransforms transforms() {
        return this.delegate.transforms();
    }

    @Override
    public TextureSlots.Data textureSlots() {
        return this.delegate.textureSlots();
    }

    @Nullable
    @Override
    public UnbakedGeometry geometry() {
        return this.delegate.geometry();
    }

    @Nullable
    @Override
    public ResourceLocation parent() {
        return this.delegate.parent();
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        this.delegate.fillAdditionalProperties(propertiesBuilder);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        this.delegate.resolveDependencies(resolver);
    }
}
