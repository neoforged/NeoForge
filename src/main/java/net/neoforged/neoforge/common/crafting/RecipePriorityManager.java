/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipePriorityManager extends SimplePreparableReloadListener<Object2IntMap<ResourceKey<Recipe<?>>>> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Logger LOGGER = LogManager.getLogger();

    private final RecipeManager recipeManager;
    private Object2IntMap<ResourceKey<Recipe<?>>> recipePriorities = Object2IntMaps.emptyMap();

    public RecipePriorityManager(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    @Override
    protected Object2IntMap<ResourceKey<Recipe<?>>> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Object2IntMap<ResourceKey<Recipe<?>>> map = new Object2IntOpenHashMap<>();
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("neoforge", "recipe_priorities.json");
        //read in all data files from neoforge:recipe_priorities in order to do layering
        for (Resource resource : resourceManager.getResourceStack(resourceLocation)) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject jsonobject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                boolean replace = GsonHelper.getAsBoolean(jsonobject, "replace", false);
                if (replace) map.clear();
                JsonObject entriesObject = GsonHelper.getAsJsonObject(jsonobject, "entries");
                for (var priorityEntry : entriesObject.entrySet()) {
                    ResourceLocation location = ResourceLocation.parse(priorityEntry.getKey());
                    int priority = priorityEntry.getValue().getAsInt();
                    map.put(ResourceKey.create(Registries.RECIPE, location), priority);
                }
            } catch (RuntimeException | IOException ioexception) {
                LOGGER.error("Couldn't read recipe priority list {} in data pack {}", resourceLocation, resource.sourcePackId(), ioexception);
            }
        }
        return Object2IntMaps.unmodifiable(map);
    }

    @Override
    protected void apply(Object2IntMap<ResourceKey<Recipe<?>>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.recipePriorities = map;
        this.recipeManager.setPriorityMap(this.recipePriorities);
        LOGGER.info("Loaded {} recipe priority overrides", this.recipePriorities.size());
    }

    /**
     * An immutable map of the registered recipe priorities in layered order.
     */
    public Object2IntMap<ResourceKey<Recipe<?>>> getRecipePriorities() {
        return this.recipePriorities;
    }
}
