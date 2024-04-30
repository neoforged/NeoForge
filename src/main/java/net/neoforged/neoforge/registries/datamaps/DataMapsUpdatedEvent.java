/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS game event bus} when the data maps of
 * a registry have either been {@linkplain UpdateCause#CLIENT_SYNC synced to the client} or {@linkplain UpdateCause#SERVER_RELOAD reloaded on the server}.
 * <p>
 * This event can be used to build caches (like weighed lists) or for post-processing the data map values. <br>
 * Remember however that the data map values should <strong>not</strong> end up referencing their owner, as they're not copied when attached to tags.
 */
public class DataMapsUpdatedEvent extends Event {
    private final RegistryAccess registryAccess;
    private final Registry<?> registry;
    private final UpdateCause cause;

    @ApiStatus.Internal
    public DataMapsUpdatedEvent(RegistryAccess registryAccess, Registry<?> registry, UpdateCause cause) {
        this.registryAccess = registryAccess;
        this.registry = registry;
        this.cause = cause;
    }

    /**
     * {@return a registry access}
     */
    public RegistryAccess getRegistries() {
        return registryAccess;
    }

    /**
     * {@return the registry that had its data maps updated}
     */
    public Registry<?> getRegistry() {
        return registry;
    }

    /**
     * {@return the key of the registry that had its data maps updated}
     */
    public ResourceKey<? extends Registry<?>> getRegistryKey() {
        return registry.key();
    }

    /**
     * Runs the given {@code consumer} if the registry is of the given {@code type}.
     * 
     * @param type     the registry key
     * @param consumer the consumer
     * @param <T>      the registry type
     */
    @SuppressWarnings("unchecked")
    public <T> void ifRegistry(ResourceKey<Registry<T>> type, Consumer<Registry<T>> consumer) {
        if (getRegistryKey() == type) {
            consumer.accept((Registry<T>) registry);
        }
    }

    /**
     * {@return the reason for the update}
     */
    public UpdateCause getCause() {
        return cause;
    }

    public enum UpdateCause {
        /**
         * The data maps have been synced to the client.
         */
        CLIENT_SYNC,
        /**
         * The data maps have been reloaded on the server.
         */
        SERVER_RELOAD
    }
}
