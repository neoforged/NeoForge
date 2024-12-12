/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.ExtendedUnbakedModel;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Extension type for the {@link UnbakedModel} interface.
 */
public interface IUnbakedModelExtension {
    private UnbakedModel self() {
        return (UnbakedModel) this;
    }

    /**
     * {@code bake} override with additional context.
     * Consider inheriting from {@link ExtendedUnbakedModel} which overrides the vanilla {@code bake} method.
     *
     * @param additionalProperties additional properties provided by NeoForge or mods
     */
    default BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        return self().bake(textures, baker, modelState, useAmbientOcclusion, usesBlockLight, itemTransforms);
    }

    /**
     * Appends additional properties for this model to the builder.
     *
     * <p>This method will already have been called on the parent models.
     * It can modify the properties added by a parent model and/or add its own.
     * This ensures that the properties are merged across the model parent-child chain.
     *
     * <p>The context map containing all the properties will be passed as the last parameter to
     * {@link #bake(TextureSlots, ModelBaker, ModelState, boolean, boolean, ItemTransforms, ContextMap)}.
     *
     * @see NeoForgeModelProperties
     */
    @ApiStatus.OverrideOnly
    default void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {}

    /**
     * Resolves additional properties by walking the model child-parent chain and calling {@link #fillAdditionalProperties(ContextMap.Builder)}.
     */
    static ContextMap getTopAdditionalProperties(UnbakedModel topModel) {
        var builder = new ContextMap.Builder();
        fillAdditionalProperties(topModel, builder);
        return builder.create(ContextKeySet.EMPTY);
    }

    private static void fillAdditionalProperties(@Nullable UnbakedModel model, ContextMap.Builder propertiesBuilder) {
        if (model != null) {
            fillAdditionalProperties(model.getParent(), propertiesBuilder);
            model.fillAdditionalProperties(propertiesBuilder);
        }
    }
}
