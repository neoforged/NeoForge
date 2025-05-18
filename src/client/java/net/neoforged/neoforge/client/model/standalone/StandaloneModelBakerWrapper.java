/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.standalone;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Wraps a {@link StandaloneModelBaker} into a {@link UnbakedStandaloneModel}.
 *
 * <p>This should not be used directly, but instead through
 * {@link ModelEvent.RegisterStandalone#register(StandaloneModelKey, StandaloneModelBaker)}.
 *
 * @param <T> The type of the baked model.
 */
@ApiStatus.Internal
public final class StandaloneModelBakerWrapper<T> implements UnbakedStandaloneModel<T> {
    private final ResourceLocation modelId;
    private final StandaloneModelBaker<T> standaloneBaker;

    public StandaloneModelBakerWrapper(ResourceLocation model, StandaloneModelBaker<T> baker) {
        this.modelId = model;
        this.standaloneBaker = baker;
    }

    @Override
    public T bake(ModelBaker baker) {
        return standaloneBaker.bake(baker.getModel(modelId), baker);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(modelId);
    }
}
