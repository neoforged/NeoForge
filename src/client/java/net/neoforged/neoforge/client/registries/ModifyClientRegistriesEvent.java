/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.registries;

import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.callback.RegistryCallback;

/// Fired during startup after builtin client registries are constructed.
/// For vanilla registries, this event is fired after vanilla entries are registered but before modded entries.
/// For modded registries, this event is fired before any entry is registered.
///
/// This event can be used to register [callbacks][IRegistryExtension#addCallback(RegistryCallback)] to the registry.
///
/// This event is not [cancellable][ICancellableEvent].
///
/// This event is fired on the mod-specific event bus, only on the [logical client][LogicalSide#CLIENT].
///
/// @see NewClientRegistryEvent
public class ModifyClientRegistriesEvent extends Event implements IModBusEvent {
    ModifyClientRegistriesEvent() {}

    /// Returns all builtin client registries.
    public Iterable<? extends Registry<?>> getRegistries() {
        return BuiltInClientRegistries.REGISTRY;
    }

    /// Retrieve a builtin registry by its key.
    ///
    /// @param key the key of the registry to retrieve
    /// @return the registry typed to the given registry key
    /// @throws IllegalArgumentException if the registry does not exist
    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        Registry<T> registry = (Registry<T>) BuiltInClientRegistries.REGISTRY.getValue(key.identifier());
        if (registry == null) {
            throw new IllegalArgumentException("No registry with key " + key);
        }
        return registry;
    }
}
