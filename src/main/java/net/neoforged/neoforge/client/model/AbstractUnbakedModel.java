/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.context.ContextMap;
import org.jetbrains.annotations.Nullable;

/**
 * Base unbaked model for custom models which support the standard top-level model parameters
 * added by vanilla and NeoForge except elements but create the quads from something other
 * than the vanilla elements spec.
 */
public abstract class AbstractUnbakedModel implements ExtendedUnbakedModel {
    /**
     * Holds the standard top-level model parameters except elements.
     * {@link UnbakedModel#bake(TextureSlots, ModelBaker, ModelState, boolean, boolean, ItemTransforms, ContextMap)}
     * must always use the values given as parameters instead of accessing this parameter directly in order to
     * take values collected along the model's parent chain into account.
     */
    protected final StandardModelParameters parameters;
    private UnbakedModel parent;

    protected AbstractUnbakedModel(StandardModelParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        if (this.parameters.parent() != null) {
            this.parent = resolver.resolve(this.parameters.parent());
        }
    }

    @Nullable
    @Override
    public Boolean getAmbientOcclusion() {
        return this.parameters.ambientOcclusion();
    }

    @Nullable
    @Override
    public GuiLight getGuiLight() {
        return this.parameters.guiLight();
    }

    @Nullable
    @Override
    public ItemTransforms getTransforms() {
        return this.parameters.itemTransforms();
    }

    @Override
    public TextureSlots.Data getTextureSlots() {
        return this.parameters.textures();
    }

    @Nullable
    @Override
    public UnbakedModel getParent() {
        return this.parent;
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        NeoForgeModelProperties.fillRootTransformProperty(propertiesBuilder, this.parameters.rootTransform());
        NeoForgeModelProperties.fillRenderTypeProperty(propertiesBuilder, this.parameters.renderTypeGroup());
        NeoForgeModelProperties.fillPartVisibilityProperty(propertiesBuilder, this.parameters.partVisibility());
    }
}
