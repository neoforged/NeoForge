/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired on the mod event bus, in order to register {@link DataMapType data map types}.
 */
public class RegisterDataMapTypesEvent extends Event implements IModBusEvent {
    private final Map<ResourceKey<Registry<?>>, Map<Identifier, DataMapType<?, ?>>> attachments;

    @ApiStatus.Internal
    public RegisterDataMapTypesEvent(Map<ResourceKey<Registry<?>>, Map<Identifier, DataMapType<?, ?>>> attachments) {
        this.attachments = attachments;
    }

    /// Register a registry data map.
    ///
    /// @param type The data map type to register
    /// @param <T>  The type of the data map
    /// @param <R>  The type of the registry
    /// @throws IllegalArgumentException      If a type with the same ID has already been registered for that registry
    /// @throws UnsupportedOperationException If the registry is a reloadable datapack registry or a non-synced "world" datapack registry and the data map is synced
    public <T, R> void register(DataMapType<R, T> type) {
        final var registry = type.registryKey();
        if (type.networkCodec() != null) {
            if (DataPackRegistriesHooks.isWorldRegistry(registry) && !DataPackRegistriesHooks.isSyncedRegistry(registry)) {
                throw new UnsupportedOperationException("Cannot register synced data map " + type.id() + " for world datapack registry " + registry.identifier() + " that is not synced!");
            } else if (DataPackRegistriesHooks.isReloadableRegistry(registry)) {
                throw new UnsupportedOperationException("Cannot register synced data map " + type.id() + " for reloadable datapack registry " + registry.identifier() + ", reloadable registries do not support sync");
            }
        }

        final var map = attachments.computeIfAbsent((ResourceKey) registry, k -> new HashMap<>());
        if (map.containsKey(type.id())) {
            throw new IllegalArgumentException("Tried to register data map type with ID " + type.id() + " to registry " + registry.identifier() + " twice");
        }
        map.put(type.id(), type);
    }
}
