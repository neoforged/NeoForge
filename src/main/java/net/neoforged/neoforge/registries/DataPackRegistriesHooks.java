/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class DataPackRegistriesHooks {
    private DataPackRegistriesHooks() {} // utility class

    private static final List<RegistryDataLoader.RegistryData<?>> NETWORKABLE_REGISTRIES = new ArrayList<>();
    private static final List<RegistryDataLoader.RegistryData<?>> DATA_PACK_REGISTRIES = new ArrayList<>(RegistryDataLoader.WORLDGEN_REGISTRIES);
    private static final List<RegistryDataLoader.RegistryData<?>> DATA_PACK_REGISTRIES_VIEW = Collections.unmodifiableList(DATA_PACK_REGISTRIES);
    private static final Set<ResourceKey<? extends Registry<?>>> SYNCED_CUSTOM_REGISTRIES = new HashSet<>();
    private static final Set<ResourceKey<? extends Registry<?>>> SYNCED_CUSTOM_REGISTRIES_VIEW = Collections.unmodifiableSet(SYNCED_CUSTOM_REGISTRIES);

    /* Internal forge hook for retaining mutable access to RegistryAccess's codec registry when it bootstraps. */
    public static List<RegistryDataLoader.RegistryData<?>> grabNetworkableRegistries(List<RegistryDataLoader.RegistryData<?>> list) {
        if (!StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().equals(RegistryDataLoader.class))
            throw new IllegalCallerException("Attempted to call DataPackRegistriesHooks#grabNetworkableRegistries!");
        List<RegistryDataLoader.RegistryData<?>> builder = new ArrayList<>(list);
        builder.addAll(NETWORKABLE_REGISTRIES);
        NETWORKABLE_REGISTRIES.clear();
        NETWORKABLE_REGISTRIES.addAll(builder);
        return Collections.unmodifiableList(NETWORKABLE_REGISTRIES);
    }

    /* Internal forge method, registers a datapack registry codec and folder. */
    static <T> void addRegistryCodec(DataPackRegistryEvent.DataPackRegistryData<T> data) {
        RegistryDataLoader.RegistryData<T> loaderData = data.loaderData();
        DATA_PACK_REGISTRIES.add(loaderData);
        if (data.networkCodec() != null) {
            SYNCED_CUSTOM_REGISTRIES.add(loaderData.key());
            NETWORKABLE_REGISTRIES.add(new RegistryDataLoader.RegistryData<T>(loaderData.key(), data.networkCodec(), false));
        }
    }

    /**
     * {@return An unmodifiable view of the list of datapack registries}.
     * These registries are loaded from per-world datapacks on server startup.
     */
    public static List<RegistryDataLoader.RegistryData<?>> getDataPackRegistries() {
        return DATA_PACK_REGISTRIES_VIEW;
    }

    public static Stream<RegistryDataLoader.RegistryData<?>> getDataPackRegistriesWithDimensions() {
        return Stream.concat(DATA_PACK_REGISTRIES_VIEW.stream(), RegistryDataLoader.DIMENSION_REGISTRIES.stream());
    }

    /**
     * {@return An unmodifiable view of the set of synced non-vanilla datapack registry IDs}
     * Clients must have each of a server's synced datapack registries to be able to connect to that server;
     * vanilla clients therefore cannot connect if this list is non-empty on the server.
     */
    public static Set<ResourceKey<? extends Registry<?>>> getSyncedCustomRegistries() {
        return SYNCED_CUSTOM_REGISTRIES_VIEW;
    }

    @Nullable
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public static <T> RegistryDataLoader.RegistryData<T> getSyncedRegistry(final ResourceKey<? extends Registry<T>> registry) {
        return (RegistryDataLoader.RegistryData<T>) NETWORKABLE_REGISTRIES.stream().filter(data -> data.key().equals(registry)).findFirst().orElse(null);
    }
}
