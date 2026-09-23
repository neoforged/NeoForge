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
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class DataPackRegistriesHooks {
    private DataPackRegistriesHooks() {} // utility class

    private static final List<RegistryDataLoader.RegistryData<?>> WORLD_REGISTRIES = new ArrayList<>(RegistryDataLoader.WORLD_REGISTRIES);
    private static final List<RegistryDataLoader.RegistryData<?>> WORLD_REGISTRIES_VIEW = Collections.unmodifiableList(WORLD_REGISTRIES);
    private static final Set<ResourceKey<? extends Registry<?>>> WORLD_REGISTRY_KEYS = new ReferenceOpenHashSet<>(WORLD_REGISTRIES.stream().map(RegistryDataLoader.RegistryData::key).toList());
    private static final List<RegistryDataLoader.RegistryData<?>> RELOADABLE_REGISTRIES = new ArrayList<>(RegistryDataLoader.RELOADABLE_REGISTRIES);
    private static final List<RegistryDataLoader.RegistryData<?>> RELOADABLE_REGISTRIES_VIEW = Collections.unmodifiableList(RELOADABLE_REGISTRIES);
    private static final Set<ResourceKey<? extends Registry<?>>> RELOADABLE_REGISTRY_KEYS = new ReferenceOpenHashSet<>(RELOADABLE_REGISTRIES.stream().map(RegistryDataLoader.RegistryData::key).toList());
    private static final List<RegistryDataLoader.RegistryData<?>> SYNCED_REGISTRIES = new ArrayList<>();
    private static final Set<ResourceKey<? extends Registry<?>>> SYNCED_REGISTRY_KEYS = new ReferenceOpenHashSet<>();

    /// Internal hook for retaining mutable access to [RegistryDataLoader]'s codec registry when it bootstraps.
    public static List<RegistryDataLoader.RegistryData<?>> captureSyncedRegistries(List<RegistryDataLoader.RegistryData<?>> list) {
        if (!StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().equals(RegistryDataLoader.class)) {
            throw new IllegalCallerException("Attempted to call DataPackRegistriesHooks#captureSyncedWorldRegistries()!");
        }

        List<RegistryDataLoader.RegistryData<?>> builder = new ArrayList<>(list);
        builder.addAll(SYNCED_REGISTRIES);
        SYNCED_REGISTRIES.clear();
        SYNCED_REGISTRIES.addAll(builder);
        SYNCED_REGISTRY_KEYS.addAll(list.stream().map(RegistryDataLoader.RegistryData::key).toList());
        return Collections.unmodifiableList(SYNCED_REGISTRIES);
    }

    static <T> void addWorldRegistry(NewDatapackRegistryEvent.RegistryData<T> data) {
        RegistryDataLoader.RegistryData<T> loaderData = data.loaderData();
        WORLD_REGISTRIES.add(loaderData);
        WORLD_REGISTRY_KEYS.add(loaderData.key());
        if (data.networkCodec() != null) {
            SYNCED_REGISTRIES.add(new RegistryDataLoader.RegistryData<>(loaderData.key(), data.networkCodec(), RegistryValidator.none()));
            SYNCED_REGISTRY_KEYS.add(loaderData.key());
        }
    }

    static <T> void addReloadableRegistry(NewDatapackRegistryEvent.RegistryData<T> data) {
        RegistryDataLoader.RegistryData<T> loaderData = data.loaderData();
        RELOADABLE_REGISTRIES.add(loaderData);
        RELOADABLE_REGISTRY_KEYS.add(loaderData.key());
    }

    /// {@return An unmodifiable view of the list of "world" datapack registries}.
    ///
    /// These registries are loaded from per-world datapacks on server startup.
    public static List<RegistryDataLoader.RegistryData<?>> getWorldRegistries() {
        return WORLD_REGISTRIES_VIEW;
    }

    /// {@return A stream of the "world" and dimension datapack registries}.
    ///
    /// These registries are loaded from per-world datapacks on server startup.
    public static Stream<RegistryDataLoader.RegistryData<?>> getWorldRegistriesWithDimensions() {
        return Stream.concat(WORLD_REGISTRIES_VIEW.stream(), RegistryDataLoader.DIMENSION_REGISTRIES.stream());
    }

    /// {@return An unmodifiable view of the list of reloadable datapack registries}.
    ///
    /// These registries are loaded from per-world datapacks on server startup and reloaded by `/reload`.
    public static List<RegistryDataLoader.RegistryData<?>> getReloadableRegistries() {
        return RELOADABLE_REGISTRIES_VIEW;
    }

    public static boolean isWorldRegistry(ResourceKey<? extends Registry<?>> registry) {
        return WORLD_REGISTRY_KEYS.contains(registry);
    }

    public static boolean isReloadableRegistry(ResourceKey<? extends Registry<?>> registry) {
        return RELOADABLE_REGISTRY_KEYS.contains(registry);
    }

    public static boolean isSyncedRegistry(ResourceKey<? extends Registry<?>> registry) {
        return SYNCED_REGISTRY_KEYS.contains(registry);
    }
}
