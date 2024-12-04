/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import java.util.HashMap;
import java.util.Map;
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
     * must always use the values given as parameters instead of accessing this parameter directly.
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
        if (this.parameters.rootTransform() != null) {
            propertiesBuilder.withParameter(NeoForgeModelProperties.TRANSFORM, this.parameters.rootTransform());
        }
        if (!this.parameters.renderTypeGroup().isEmpty()) {
            propertiesBuilder.withParameter(NeoForgeModelProperties.RENDER_TYPE, this.parameters.renderTypeGroup());
        }
        if (!this.parameters.partVisibility().isEmpty()) {
            Map<String, Boolean> visibility = propertiesBuilder.getOptionalParameter(NeoForgeModelProperties.PART_VISIBILITY);
            if (visibility != null) {
                visibility = new HashMap<>(visibility);
                visibility.putAll(this.parameters.partVisibility());
                visibility = Map.copyOf(visibility);
            } else {
                visibility = this.parameters.partVisibility();
            }
            propertiesBuilder.withParameter(NeoForgeModelProperties.PART_VISIBILITY, visibility);
        }
    }
}
