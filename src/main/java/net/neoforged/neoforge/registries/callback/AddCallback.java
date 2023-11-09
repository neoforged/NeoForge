/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.callback;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Fired when objects are added to the registry. This will fire when the registry is rebuilt
 * on the client side from a server side synchronization.
 */
@FunctionalInterface
public non-sealed interface AddCallback<T> extends RegistryCallback<T> {
    /**
     * Called when an entry is added to the registry.
     *
     * @param registry the registry
     * @param id       the integer ID assigned to the entry
     * @param key      the resource key for the entry
     * @param value    the entry's value
     */
    void onAdd(Registry<T> registry, int id, ResourceKey<T> key, T value);
}
