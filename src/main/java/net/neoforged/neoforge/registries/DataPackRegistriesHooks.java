/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class DataPackRegistriesHooks {
    private DataPackRegistriesHooks() {} // utility class

    private static final DatapackRegistryGroup WORLD_REGISTRIES = new DatapackRegistryGroup();
    private static final DatapackRegistryGroup RELOADABLE_REGISTRIES = new DatapackRegistryGroup();
    private static final DatapackRegistryGroup SYNCED_REGISTRIES = new DatapackRegistryGroup();

    /// Internal hook for retaining mutable access to [RegistryDataLoader]'s world registry list when it bootstraps.
    public static List<RegistryDataLoader.RegistryData<?>> captureWorldRegistries(List<RegistryDataLoader.RegistryData<?>> list) {
        return WORLD_REGISTRIES.init(list);
    }

    /// Internal hook for retaining mutable access to [RegistryDataLoader]'s reloadable registry list when it bootstraps.
    public static List<RegistryDataLoader.RegistryData<?>> captureReloadableRegistries(List<RegistryDataLoader.RegistryData<?>> list) {
        return RELOADABLE_REGISTRIES.init(list);
    }

    /// Internal hook for retaining mutable access to [RegistryDataLoader]'s synced registry list when it bootstraps.
    public static List<RegistryDataLoader.RegistryData<?>> captureSyncedRegistries(List<RegistryDataLoader.RegistryData<?>> list) {
        return SYNCED_REGISTRIES.init(list);
    }

    static <T> void addWorldRegistry(NewDatapackRegistryEvent.RegistryData<T> data) {
        WORLD_REGISTRIES.add(data.loaderData());
        if (data.networkCodec() != null) {
            SYNCED_REGISTRIES.add(new RegistryDataLoader.RegistryData<>(data.loaderData().key(), data.networkCodec(), RegistryValidator.none()));
        }
    }

    static <T> void addReloadableRegistry(NewDatapackRegistryEvent.RegistryData<T> data) {
        RELOADABLE_REGISTRIES.add(data.loaderData());
    }

    /// {@return a stream of the "world" and dimension datapack registries}
    ///
    /// These registries are loaded from per-world datapacks on server startup.
    ///
    /// This method is used in datagen ([RegistryPatchGenerator], [RegistriesDatapackGenerator], [DatapackBuiltinEntriesProvider]) in place
    /// of [RegistryDataLoader#WORLD_REGISTRIES] to ensure mods can datagen [dimensions][RegistryDataLoader#DIMENSION_REGISTRIES].
    public static Stream<RegistryDataLoader.RegistryData<?>> getWorldRegistriesWithDimensions() {
        return Stream.concat(WORLD_REGISTRIES.registryData.stream(), RegistryDataLoader.DIMENSION_REGISTRIES.stream());
    }

    public static boolean isWorldRegistry(ResourceKey<? extends Registry<?>> registry) {
        return WORLD_REGISTRIES.registryKeys.contains(registry);
    }

    public static boolean isReloadableRegistry(ResourceKey<? extends Registry<?>> registry) {
        return RELOADABLE_REGISTRIES.registryKeys.contains(registry);
    }

    public static boolean isSyncedRegistry(ResourceKey<? extends Registry<?>> registry) {
        return SYNCED_REGISTRIES.registryKeys.contains(registry);
    }

    static {
        // Force static init of RegistryDataLoader
        RegistryDataLoader.WORLD_REGISTRIES.size();
    }

    private record DatapackRegistryGroup(List<RegistryDataLoader.RegistryData<?>> registryData, Set<ResourceKey<? extends Registry<?>>> registryKeys) {
        DatapackRegistryGroup() {
            this(new ArrayList<>(), new ReferenceOpenHashSet<>());
        }

        List<RegistryDataLoader.RegistryData<?>> init(List<RegistryDataLoader.RegistryData<?>> vanillaData) {
            vanillaData.reversed().forEach(this.registryData::addFirst);
            vanillaData.stream().map(RegistryDataLoader.RegistryData::key).forEach(this.registryKeys::add);
            return Collections.unmodifiableList(this.registryData);
        }

        void add(RegistryDataLoader.RegistryData<?> data) {
            this.registryData.add(data);
            this.registryKeys.add(data.key());
        }
    }
}
