/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import org.jetbrains.annotations.ApiStatus;

/**
 * Extension for {@link UnbakedModel}.
 */
public interface UnbakedModelExtension extends ResolvableModel {
    /**
     * Appends additional properties for this model to the builder.
     *
     * <p>This method will already have been called on the parent models.
     * It can modify the properties added by a parent model and/or add its own.
     * This ensures that the properties are merged across the model parent-child chain.
     *
     * <p>The context map containing all the properties can be retrieved later
     * using {@link ResolvedModel#getTopAdditionalProperties()}.
     *
     * @see NeoForgeModelProperties
     */
    @ApiStatus.OverrideOnly
    default void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {}

    /**
     * Can be overridden to request the resolution of additional models.
     * Use {@link Resolver#markDependency(ResourceLocation)} to mark dependencies,
     * then retrieve them later from {@link ModelBaker#getModel(ResourceLocation)}.
     */
    @Override
    default void resolveDependencies(Resolver resolver) {}
}
