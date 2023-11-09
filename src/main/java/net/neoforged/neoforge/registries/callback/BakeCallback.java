/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.callback;

import net.minecraft.core.Registry;

/**
 * Fired when the registry is finished with all registration.
 */
@FunctionalInterface
public non-sealed interface BakeCallback<T> extends RegistryCallback<T> {
    /**
     * Called when the registry is frozen, and all registration is finished.
     *
     * @param registry the registry
     */
    void onBake(Registry<T> registry);
}
