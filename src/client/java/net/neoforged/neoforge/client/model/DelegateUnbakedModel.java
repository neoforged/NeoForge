/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import org.jspecify.annotations.Nullable;

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
    public Identifier parent() {
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
