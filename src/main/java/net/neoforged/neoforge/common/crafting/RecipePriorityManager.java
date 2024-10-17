/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipePriorityManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Logger LOGGER = LogManager.getLogger();

    private Object2IntMap<ResourceLocation> recipePriorities = Object2IntMaps.emptyMap();
    private static final String folder = "recipe_priorities";

    public RecipePriorityManager() {
        super(GSON, folder);
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, JsonElement> map = super.prepare(resourceManager, profilerFiller);
        for (String namespace : resourceManager.getNamespaces()) {
            ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(namespace, "recipe_priorities/recipe_priorities.json");
            Optional<Resource> resource = resourceManager.getResource(resourceLocation);
            if (resource.isPresent()) {
                try (Reader reader = resource.get().openAsReader()) {
                    JsonObject jsonobject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                    boolean replace = GsonHelper.getAsBoolean(jsonobject, "replace", false);
                    if (replace)
                        map.clear();
                    JsonObject entries = GsonHelper.getAsJsonObject(jsonobject, "entries");
                    for (String entry : entries.keySet()) {
                        ResourceLocation loc = ResourceLocation.tryParse(entry);
                        map.remove(loc); //remove and re-add if needed, to update the ordering.
                        map.put(loc, entries.get(entry));
                    }
                } catch (RuntimeException | IOException ioexception) {
                    LOGGER.error("Couldn't read recipe priority list {} in data pack {}", resourceLocation, resource.get().sourcePackId(), ioexception);
                }
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        Object2IntMap<ResourceLocation> builder = new Object2IntOpenHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            JsonElement json = entry.getValue();
            if (json instanceof JsonObject jsonObject) {
                JsonElement entries = jsonObject.get("entries");
                if (entries instanceof JsonObject entriesObject) {
                    for (var priorityEntry : entriesObject.entrySet()) {
                        ResourceLocation location = ResourceLocation.tryParse(priorityEntry.getKey());
                        int priority = priorityEntry.getValue().getAsInt();
                        if (location != null) {
                            builder.put(location, priority);
                        }
                    }
                }
            }
        }
        this.recipePriorities = Object2IntMaps.unmodifiable(builder);
        LOGGER.info("Loaded {} recipe priority overrides", this.recipePriorities.size());
    }

    /**
     * An immutable map of the registered recipe priorities in layered order.
     */
    public Object2IntMap<ResourceLocation> getRecipePriorities() {
        return this.recipePriorities;
    }
}
