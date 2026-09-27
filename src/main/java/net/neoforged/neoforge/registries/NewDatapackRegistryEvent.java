/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.event.IModBusEvent;
import org.jspecify.annotations.Nullable;

/// Fired when datapack registries can be registered.
/// Datapack registries are registries which can only load entries through JSON files from datapacks.
///
/// Data JSONs will be loaded from `data/<datapack_namespace>/modid/registryname/`, where `modid` is the namespace of the registry key.
///
/// This event is not [cancellable][ICancellableEvent].
///
/// This event is fired on the mod-specific event bus, on both [physical sides][Dist].
public final class NewDatapackRegistryEvent extends Event implements IModBusEvent {
    private final List<RegistryData<?>> worldRegistries = new ArrayList<>();
    private final List<RegistryData<?>> reloadableRegistries = new ArrayList<>();

    NewDatapackRegistryEvent() {}

    /// Registers the given registry key as an unsynced world datapack registry, which will cause data to be loaded from
    /// a datapack folder based on the registry's name. The datapack registry is not required to be present
    /// on clients when connecting to servers with the mod/registry.
    ///
    /// Data JSONs will be loaded from `data/<datapack_namespace>/modid/registryname/`, where `modid` is the namespace of the registry key.
    ///
    /// World registries are only loaded once during server startup.
    ///
    /// @param registryKey The root registry key of the new datapack registry
    /// @param codec       The codec to be used for loading data from datapacks on servers
    /// @see #worldRegistry(ResourceKey, Codec, Codec)
    public <T> void worldRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
        this.worldRegistry(registryKey, codec, null);
    }

    /// Registers the registry key as a world datapack registry, which will cause data to be loaded from
    /// a datapack folder based on the registry's name.
    ///
    /// Data JSONs will be loaded from `data/<datapack_namespace>/modid/registryname/`, where `modid` is the namespace of the registry key.
    ///
    /// World registries are only loaded once during server startup.
    ///
    /// @param registryKey  The root registry key of the new datapack registry
    /// @param codec        The codec to be used for loading data from datapacks on servers
    /// @param networkCodec The codec to be used for syncing loaded data to clients.
    ///                     If `networkCodec` is null, data will not be synced, and clients are not required to have this
    ///                     datapack registry to join a server.
    ///
    ///                     If `networkCodec` is not null, clients must have this datapack registry/mod
    ///                     when joining a server that has this datapack registry/mod.
    ///                     The data will be synced using the network codec and accessible via [ClientPacketListener#registryAccess()].
    /// @see #worldRegistry(ResourceKey, Codec)
    public <T> void worldRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec) {
        this.worldRegistries.add(new RegistryData<>(registryKey, codec, networkCodec));
    }

    /// Registers the registry key as a world datapack registry with a [RegistryBuilder] configurator, which will cause data to be loaded from
    /// a datapack folder based on the registry's name.
    ///
    /// Data JSONs will be loaded from `data/<datapack_namespace>/modid/registryname/`, where `modid` is the namespace of the registry key.
    ///
    /// World registries are only loaded once during server startup.
    ///
    /// @param registryKey  The root registry key of the new datapack registry
    /// @param codec        The codec to be used for loading data from datapacks on servers
    /// @param networkCodec The codec to be used for syncing loaded data to clients.
    ///                     If `networkCodec` is null, data will not be synced, and clients are not required to have this
    ///                     datapack registry to join a server.
    ///
    ///                     If `networkCodec` is not null, clients must have this datapack registry/mod
    ///                     when joining a server that has this datapack registry/mod.
    ///                     The data will be synced using the network codec and accessible via [ClientPacketListener#registryAccess()].
    /// @param consumer     A consumer that configures the provided RegistryBuilder
    /// @see #worldRegistry(ResourceKey, Codec)
    /// @see #worldRegistry(ResourceKey, Codec, Codec)
    public <T> void worldRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec, Consumer<RegistryBuilder<T>> consumer) {
        this.worldRegistries.add(new RegistryData<>(registryKey, codec, networkCodec, consumer));
    }

    /// Registers the given registry key as a reloadable datapack registry, which will cause data to be loaded from
    /// a datapack folder based on the registry's name.
    ///
    /// Data JSONs will be loaded from `data/<datapack_namespace>/modid/registryname/`, where `modid` is the namespace of the registry key.
    ///
    /// Reloadable registries are loaded on server startup and by the `/reload` command.
    ///
    /// @param registryKey The root registry key of the new datapack registry
    /// @param codec       The codec to be used for loading data from datapacks on servers
    /// @see #reloadableRegistry(ResourceKey, Codec, Consumer)
    public <T> void reloadableRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
        this.reloadableRegistries.add(new RegistryData<>(registryKey, codec, null));
    }

    /// Registers the given registry key as a reloadable datapack registry with a [RegistryBuilder] configurator, which will cause data to be loaded from
    /// a datapack folder based on the registry's name.
    ///
    /// Data JSONs will be loaded from `data/<datapack_namespace>/modid/registryname/`, where `modid` is the namespace of the registry key.
    ///
    /// Reloadable registries are loaded on server startup and by the `/reload` command.
    ///
    /// @param registryKey The root registry key of the new datapack registry
    /// @param codec       The codec to be used for loading data from datapacks on servers
    /// @param consumer    A consumer that configures the provided RegistryBuilder
    /// @see #reloadableRegistry(ResourceKey, Codec)
    public <T> void reloadableRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, Consumer<RegistryBuilder<T>> consumer) {
        this.reloadableRegistries.add(new RegistryData<>(registryKey, codec, null, consumer));
    }

    void process() {
        for (RegistryData<?> registryData : this.worldRegistries) {
            DataPackRegistriesHooks.addWorldRegistry(registryData);
        }
        for (RegistryData<?> registryData : this.reloadableRegistries) {
            DataPackRegistriesHooks.addReloadableRegistry(registryData);
        }
    }

    record RegistryData<T>(RegistryDataLoader.RegistryData<T> loaderData, @Nullable Codec<T> networkCodec) {
        RegistryData(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec) {
            this(new RegistryDataLoader.RegistryData<>(registryKey, codec, RegistryValidator.none()), networkCodec);
        }

        RegistryData(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec, Consumer<RegistryBuilder<T>> consumer) {
            this(new RegistryDataLoader.RegistryData<>(registryKey, codec, RegistryValidator.none(), consumer), networkCodec);
        }
    }
}
