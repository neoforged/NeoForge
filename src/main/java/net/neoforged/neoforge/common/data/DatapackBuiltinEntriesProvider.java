/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.resources.RegistryDataLoader;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;

/**
 * An extension of the {@link RegistriesDatapackGenerator} which properly handles
 * referencing existing dynamic registry objects within another dynamic registry
 * object.
 */
public class DatapackBuiltinEntriesProvider extends RegistriesDatapackGenerator {
    private final CompletableFuture<HolderLookup.Provider> fullRegistries;

    /**
     * Constructs a new datapack provider which generates all registry objects
     * from the provided mods using the holder.
     *
     * @param output     the target directory of the data generator
     * @param registries a future of a lookup for registries and their objects
     * @param modIds     a set of mod ids to generate the dynamic registry objects of
     */
    public DatapackBuiltinEntriesProvider(PackOutput output, String name, Collection<RegistryDataLoader.RegistryData<?>> registryData, CompletableFuture<RegistrySetBuilder.PatchedRegistries> registries, Set<String> modIds) {
        super(output, name, registryData, registries.thenApply(RegistrySetBuilder.PatchedRegistries::patchesWithConditions), modIds);
        this.fullRegistries = registries.thenApply(RegistrySetBuilder.PatchedRegistries::full);
    }

    public static DatapackBuiltinEntriesProvider forWorldLayer(PackOutput output, String name, CompletableFuture<HolderLookup.Provider> worldRegistries, RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        return new DatapackBuiltinEntriesProvider(output, name, DataPackRegistriesHooks.getDataPackRegistries(), RegistryPatchGenerator.createWorldLookup(worldRegistries, entriesBuilder), modIds);
    }

    public static DatapackBuiltinEntriesProvider forReloadableLayer(PackOutput output, String name, CompletableFuture<HolderLookup.Provider> worldRegistries, CompletableFuture<HolderLookup.Provider> reloadableRegistries, RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        return new DatapackBuiltinEntriesProvider(output, name, RegistryDataLoader.RELOADABLE_REGISTRIES, RegistryPatchGenerator.createReloadableLookup(worldRegistries, reloadableRegistries, entriesBuilder), modIds);
    }

    /**
     * Get the registry holder lookup provider that includes elements added from the {@link RegistrySetBuilder}
     */
    public CompletableFuture<HolderLookup.Provider> getRegistryProvider() {
        return fullRegistries;
    }
}
