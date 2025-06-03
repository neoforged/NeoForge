/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.standalone;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * A key referring to a model file to be loaded and baked as a standalone model (not bound to a block or item).
 * <p>
 * This key is registered together with a {@link StandaloneModelBaker} or {@link UnbakedStandaloneModel} in
 * {@link ModelEvent.RegisterStandalone} and later used to retrieve the model baked by the {@link StandaloneModelBaker},
 * using {@link ModelManager#getStandaloneModel(StandaloneModelKey)}.
 * <p>
 * The key is compared by identity as multiple keys may refer to the same model file while using different bakers.
 *
 * @param <T> The type returned by the {@link StandaloneModelBaker} this key is registered with
 */
public final class StandaloneModelKey<T> {
    private final ResourceLocation modelId;

    public StandaloneModelKey(ResourceLocation modelId) {
        this.modelId = modelId;
    }

    public ResourceLocation getModelId() {
        return this.modelId;
    }

    @Override
    public String toString() {
        return "StandaloneModelKey[modelId=" + this.modelId + ']';
    }
}
