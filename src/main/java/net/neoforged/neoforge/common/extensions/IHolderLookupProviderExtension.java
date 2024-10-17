/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

public interface IHolderLookupProviderExtension {
    private HolderLookup.Provider self() {
        return (HolderLookup.Provider) this;
    }

    /**
     * Shortcut method to get a holder from a ResourceKey.
     * 
     * @throws IllegalStateException if the registry or key is not found.
     */
    default <T> Holder<T> holderOrThrow(ResourceKey<T> key) {
        return this.self().lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    /**
     * Shortcut method to get an optional holder from a ResourceKey.
     */
    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        Optional<? extends HolderLookup.RegistryLookup<T>> registry = this.self().lookup(key.registryKey());
        return registry.flatMap(tRegistryLookup -> tRegistryLookup.get(key));
    }
}
