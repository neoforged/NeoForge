/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.registries.callback.RegistryCallback;

/**
 * Fired during startup after builtin registries are constructed.
 * For vanilla registries, this event is fired after vanilla entries are registered but before modded entries.
 * For modded registries, this event is fired before any entry is registered.
 *
 * <p>This event can be used to register {@linkplain IRegistryExtension#addCallback(RegistryCallback) callbacks} to the registry.
 *
 * <p>This event cannot be used to modify datapack registries.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
 * <p>This event is fired on the {@linkplain FMLModContainer#getEventBus() mod-specific event bus},
 * on both {@linkplain LogicalSide logical sides}.</p>
 *
 * @see NewRegistryEvent
 */
public class ModifyRegistriesEvent extends Event implements IModBusEvent {
    ModifyRegistriesEvent() {}

    /**
     * Returns all builtin registries.
     */
    public Iterable<? extends Registry<?>> getRegistries() {
        return BuiltInRegistries.REGISTRY;
    }

    /**
     * Retrieve a builtin registry by its key.
     *
     * @param key the key of the registry to retrieve
     * @return the registry typed to the given registry key
     * @throws IllegalArgumentException if the registry does not exist
     */
    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
        if (registry == null) {
            throw new IllegalArgumentException("No registry with key " + key);
        }
        return registry;
    }
}
