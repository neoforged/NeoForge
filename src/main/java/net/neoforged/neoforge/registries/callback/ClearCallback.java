/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.callback;

import net.minecraft.core.Registry;

/**
 * Fired when the registry is cleared.
 * This is done before a registry is reloaded from client or server.
 */
@FunctionalInterface
public non-sealed interface ClearCallback<T> extends RegistryCallback<T> {
    /**
     * Called when the registry is cleared before anything is done to the registry.
     *
     * @param registry the registry
     * @param full     if {@code true}, all entries in the registry will be cleared.
     *                 if {@code false}, only integer IDs in the registry will be cleared.
     */
    void onClear(Registry<T> registry, boolean full);
}
