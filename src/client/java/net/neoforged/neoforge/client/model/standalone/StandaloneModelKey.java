/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.standalone;

import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelManager;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * A key referring to a model file to be loaded and baked as a standalone model (not bound to a block or item).
 * <p>
 * This key is registered together with a {@link SimpleUnbakedStandaloneModel} or {@link UnbakedStandaloneModel} in
 * {@link ModelEvent.RegisterStandalone} and later used to retrieve the model baked by the {@link SimpleUnbakedStandaloneModel},
 * using {@link ModelManager#getStandaloneModel(StandaloneModelKey)}.
 * <p>
 * The key is compared by identity as multiple keys may refer to the same model while using different bakers.
 *
 * @param <T> The type returned by the {@link SimpleUnbakedStandaloneModel} this key is registered with
 */
public final class StandaloneModelKey<T> {
    private final ModelDebugName name;

    public StandaloneModelKey(ModelDebugName name) {
        this.name = name;
    }

    public String getName() {
        return this.name.debugName();
    }

    @Override
    public String toString() {
        return "StandaloneModelKey[name=" + this.name.debugName() + ']';
    }
}
