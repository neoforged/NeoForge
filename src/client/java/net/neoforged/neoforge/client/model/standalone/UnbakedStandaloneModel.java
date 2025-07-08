/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.standalone;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ResolvableModel;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * An unbaked {@linkplain StandaloneModelKey standalone model}.
 *
 * <p>Similar to {@link ItemModel.Unbaked} and other {@link ResolvableModel}s, this model can
 * {@linkplain ResolvableModel.Resolver#resolveDependencies(Resolver) depend} on one or more model files, and then
 * combine them into a single baked model.
 *
 * <p>The baked object can be retrieved later using {@link ModelManager#getStandaloneModel(StandaloneModelKey)}.
 *
 * <p>{@link SimpleUnbakedStandaloneModel} provides a basic implementation that loads a single model.
 *
 * @param <T> The type of the baked model.
 * @see ModelEvent.RegisterStandalone#register(StandaloneModelKey, UnbakedStandaloneModel)
 */
public interface UnbakedStandaloneModel<T> extends ResolvableModel {
    /**
     * Bake this model.
     *
     * @param baker The current model baker.
     * @return The fully-baked model.
     */
    T bake(ModelBaker baker);
}
