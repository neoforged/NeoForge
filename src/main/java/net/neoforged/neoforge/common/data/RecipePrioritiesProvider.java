/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Data Provider class for generating the recipe_priorities.json file and adding recipe priority overrides to it.
 * These overrides allow a specified recipe to be given a certain load priority relative to another recipe, by providing the recipe ID and the integer representing the priority.
 * All recipes are treated with a priority of 0 by default. Recipes assigned with higher priority values will take precedent over lower priority values when choosing what recipe result to provide in a crafting block.
 * E.g. A recipe given the priority of 1 that has matching ingredients to a recipe with a priority of 0 will output its result instead of the result of the recipe with priority 0.
 */
public abstract class RecipePrioritiesProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;
    protected HolderLookup.Provider registries;
    private final String modid;
    private final Map<ResourceLocation, Integer> toSerialize = new LinkedHashMap<>();
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
        this.start();

        Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("neoforge").resolve("recipe_priorities.json");

        JsonObject entries = new JsonObject();
        this.toSerialize.forEach((key, value) -> entries.addProperty(key.toString(), value));

        JsonObject json = new JsonObject();
        if (this.replace) {
            json.addProperty("replace", true);
        }
        json.add("entries", entries);

        return DataProvider.saveStable(cache, json, path);
    }

    public void add(ResourceLocation recipe, int priority) {
        this.toSerialize.put(recipe, priority);
    }

    public void add(ResourceKey<Recipe> recipe, int priority) {
        this.add(recipe.location(), priority);
    }

    public void add(String recipe, int priority) {
        this.add(ResourceLocation.fromNamespaceAndPath(this.modid, recipe), priority);
    }

    @Override
    public String getName() {
        return "Recipe Priorities : " + this.modid;
    }
}
