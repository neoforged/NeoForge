/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Data Provider for the GlobalLootModifier system. See {@link LootModifier}
 *
 * This provider only requires implementing {@link #start()} and calling {@link #add} from it.
 */
public abstract class GlobalLootModifierProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;
    protected HolderLookup.Provider registries;
    private final String modid;
    private final Map<String, WithConditions<IGlobalLootModifier>> toSerialize = new LinkedHashMap<>();

    public GlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        this.output = output;
        this.registriesLookup = registries;
        this.modid = modid;
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

        Path modifierFolderPath = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(this.modid).resolve("loot_modifiers");
        List<Identifier> entries = new ArrayList<>();

        ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();

        for (var entry : toSerialize.entrySet()) {
            var name = entry.getKey();
            var lootModifier = entry.getValue();
            entries.add(Identifier.fromNamespaceAndPath(modid, name));
            Path modifierPath = modifierFolderPath.resolve(name + ".json");
            futuresBuilder.add(DataProvider.saveStable(cache, registries, IGlobalLootModifier.CONDITIONAL_CODEC, Optional.of(lootModifier), modifierPath));
        }

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
        this.toSerialize.put(modifier, new WithConditions<>(conditions, instance));
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
