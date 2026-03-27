/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import org.jspecify.annotations.Nullable;

/**
 * Base unbaked model for custom models which support the standard top-level model parameters
 * added by vanilla and NeoForge except elements but create the quads from something other
 * than the vanilla elements spec.
 */
public abstract class AbstractUnbakedModel implements UnbakedModel {
    /**
     * Holds the standard top-level model parameters except elements.
     * {@link UnbakedGeometry#bake(TextureSlots, ModelBaker, ModelState, ModelDebugName, ContextMap)}
     * must always use the values given as parameters instead of accessing this parameter directly in order to
     * take values collected along the model's parent chain into account.
     */
    protected final StandardModelParameters parameters;

    protected AbstractUnbakedModel(StandardModelParameters parameters) {
        this.parameters = parameters;
    }

    @Nullable
    @Override
    public Boolean ambientOcclusion() {
        return this.parameters.ambientOcclusion();
    }

    @Nullable
    @Override
    public GuiLight guiLight() {
        return this.parameters.guiLight();
    }

    @Nullable
    @Override
    public ItemTransforms transforms() {
        return this.parameters.itemTransforms();
    }

    @Override
    public TextureSlots.Data textureSlots() {
        return this.parameters.textures();
    }

    @Nullable
    @Override
    public Identifier parent() {
        return this.parameters.parent();
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        NeoForgeModelProperties.fillRootTransformProperty(propertiesBuilder, this.parameters.rootTransform());
        NeoForgeModelProperties.fillPartVisibilityProperty(propertiesBuilder, this.parameters.partVisibility());
    }
}
