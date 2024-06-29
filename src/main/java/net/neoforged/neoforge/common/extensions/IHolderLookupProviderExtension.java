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
    default HolderLookup.Provider self() {
        return (HolderLookup.Provider) this;
    }

    default <T> Holder<T> holderOrThrow(ResourceKey<T> key) {
        return this.self().lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        Optional<HolderLookup.RegistryLookup<T>> registry = this.self().lookup(key.registryKey());
        return registry.flatMap(tRegistryLookup -> tRegistryLookup.get(key));
    }
}
