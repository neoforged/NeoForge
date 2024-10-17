/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public abstract class RecipePrioritiesProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;
    protected HolderLookup.Provider registries;
    private final String modid;
    private final Map<ResourceLocation, Integer> toSerialize = new HashMap<>();
    private boolean replace = false;

    public RecipePrioritiesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        this.output = output;
        this.registriesLookup = registries;
        this.modid = modid;
    }

    /**
     * Sets the "replace" key in recipe_priorities to true.
     */
    protected void replacing() {
        this.replace = true;
    }

    /**
     * Call {@link #add} here, which will pass in the necessary information to write the jsons.
     */
    protected abstract void start();

    @Override
    public final CompletableFuture<?> run(CachedOutput cache) {
        return this.registriesLookup.thenCompose(registries -> this.run(cache, registries));
    }

    protected CompletableFuture<?> run(CachedOutput cache, HolderLookup.Provider registries) {
        this.registries = registries;
        start();

        Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(this.modid).resolve("recipe_priorities").resolve("recipe_priorities.json");

        JsonObject entries = new JsonObject();
        toSerialize.forEach((key, value) -> entries.addProperty(key.toString(), value));

        JsonObject json = new JsonObject();
        json.addProperty("replace", this.replace);
        json.add("entries", entries);

        ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();
        futuresBuilder.add(DataProvider.saveStable(cache, json, path));

        return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
    }

    public void add(ResourceLocation recipe, Integer priority) {
        this.toSerialize.put(recipe, priority);
    }

    public void add(String recipe, Integer priority) {
        add(ResourceLocation.fromNamespaceAndPath(this.modid, recipe), priority);
    }

    public void add(String id, String location, Integer priority) {
        add(ResourceLocation.fromNamespaceAndPath(id, location), priority);
    }

    @Override
    public String getName() {
        return "Recipe Priorities : " + modid;
    }
}
