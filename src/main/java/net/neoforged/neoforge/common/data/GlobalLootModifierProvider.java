/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Provider for forge's GlobalLootModifier system. See {@link LootModifier}
 *
 * This provider only requires implementing {@link #start()} and calling {@link #add} from it.
 */
public abstract class GlobalLootModifierProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput output;
    private final String modid;
    private final Map<String, JsonElement> toSerialize = new HashMap<>();
    private boolean replace = false;

    public GlobalLootModifierProvider(PackOutput output, String modid) {
        this.output = output;
        this.modid = modid;
    }

    /**
     * Sets the "replace" key in global_loot_modifiers to true.
     */
    protected void replacing() {
        this.replace = true;
    }

    /**
     * Call {@link #add} here, which will pass in the necessary information to write the jsons.
     */
    protected abstract void start();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        start();

        Path forgePath = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("neoforge").resolve("loot_modifiers").resolve("global_loot_modifiers.json");
        Path modifierFolderPath = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(this.modid).resolve("loot_modifiers");
        List<ResourceLocation> entries = new ArrayList<>();

        ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();

        toSerialize.forEach(LamdbaExceptionUtils.rethrowBiConsumer((name, json) -> {
            entries.add(new ResourceLocation(modid, name));
            Path modifierPath = modifierFolderPath.resolve(name + ".json");
            futuresBuilder.add(DataProvider.saveStable(cache, json, modifierPath));
        }));

        JsonObject forgeJson = new JsonObject();
        forgeJson.addProperty("replace", this.replace);
        forgeJson.add("entries", GSON.toJsonTree(entries.stream().map(ResourceLocation::toString).collect(Collectors.toList())));

        futuresBuilder.add(DataProvider.saveStable(cache, forgeJson, forgePath));

        return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
    }

    /**
     * Passes in the data needed to create the file without any extra objects.
     *
     * @param modifier   the name of the modifier, which will be the file name
     * @param instance   the instance to serialize
     * @param conditions a list of conditions to add to the GLM file
     */
    public <T extends IGlobalLootModifier> void add(String modifier, T instance, List<ICondition> conditions) {
        JsonElement json = IGlobalLootModifier.CONDITIONAL_CODEC.encodeStart(JsonOps.INSTANCE, Optional.of(new WithConditions<>(conditions, instance))).getOrThrow(false, s -> {});
        this.toSerialize.put(modifier, json);
    }

    /**
     * Passes in the data needed to create the file without any extra objects.
     *
     * @param modifier   the name of the modifier, which will be the file name
     * @param instance   the instance to serialize
     * @param conditions a list of conditions to add to the GLM file
     */
    public <T extends IGlobalLootModifier> void add(String modifier, T instance, ICondition... conditions) {
        add(modifier, instance, Arrays.asList(conditions));
    }

    @Override
    public String getName() {
        return "Global Loot Modifiers : " + modid;
    }
}
