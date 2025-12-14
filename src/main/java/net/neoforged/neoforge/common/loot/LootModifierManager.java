/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootModifierManager extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Logger LOGGER = LogManager.getLogger();

    private Map<Identifier, IGlobalLootModifier> registeredLootModifiers = ImmutableMap.of();
    private static final String folder = "loot_modifiers";

    public LootModifierManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(folder));
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<Identifier, JsonElement> map = super.prepare(resourceManager, profilerFiller);
        List<Identifier> finalLocations = new ArrayList<>();
        Identifier resourceLocation = Identifier.fromNamespaceAndPath("neoforge", "loot_modifiers/global_loot_modifiers.json");
        //read in all data files from neoforge:loot_modifiers/global_loot_modifiers in order to do layering
        for (Resource resource : resourceManager.getResourceStack(resourceLocation)) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject jsonobject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                boolean replace = GsonHelper.getAsBoolean(jsonobject, "replace", false);
                if (replace)
                    finalLocations.clear();
                JsonArray entries = GsonHelper.getAsJsonArray(jsonobject, "entries");
                for (int i = 0; i < entries.size(); i++) {
                    Identifier loc = Identifier.parse(GsonHelper.convertToString(entries.get(i), "entries[" + i + "]"));
                    finalLocations.remove(loc); //remove and re-add if needed, to update the ordering.
                    finalLocations.add(loc);
                }
            } catch (RuntimeException | IOException ioexception) {
                LOGGER.error("Couldn't read global loot modifier list {} in data pack {}", resourceLocation, resource.sourcePackId(), ioexception);
            }
        }
        Map<Identifier, JsonElement> finalMap = new HashMap<>();
        //use layered config to fetch modifier data files (modifiers missing from config are disabled)
        for (Identifier location : finalLocations) {
            finalMap.put(location, map.get(location));
        }
        return finalMap;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resourceList, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        DynamicOps<JsonElement> ops = this.makeConditionalOps();
        Builder<Identifier, IGlobalLootModifier> builder = ImmutableMap.builder();
        for (Map.Entry<Identifier, JsonElement> entry : resourceList.entrySet()) {
            Identifier location = entry.getKey();
            JsonElement json = entry.getValue();
            IGlobalLootModifier.CONDITIONAL_CODEC.parse(ops, json)
                    // log error if parse fails
                    .resultOrPartial(errorMsg -> LOGGER.warn("Could not decode GlobalLootModifier with json id {} - error: {}", location, errorMsg))
                    // add loot modifier if parse succeeds
                    .flatMap(Function.identity())
                    .ifPresent(carrier -> builder.put(location, carrier.carrier()));
        }
        this.registeredLootModifiers = builder.build();
    }

    /**
     * An immutable collection of the registered loot modifiers in layered order.
     */
    public Collection<IGlobalLootModifier> getAllLootMods() {
        return registeredLootModifiers.values();
    }
}
