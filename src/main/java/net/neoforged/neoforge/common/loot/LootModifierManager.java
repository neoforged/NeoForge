/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootModifierManager extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final String FOLDER = "loot_modifiers";

    private Map<Identifier, IGlobalLootModifier> registeredLootModifiers = ImmutableMap.of();
    private List<IGlobalLootModifier> sortedModifiers = List.of();

    public LootModifierManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(FOLDER));
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
        this.sortedModifiers = this.registeredLootModifiers.values().stream()
                .sorted(Comparator.comparingInt(IGlobalLootModifier::priority))
                .toList();
    }

    /**
     * Returns an iterable view of all loot modifiers, sorted in the order they should be applied.
     */
    public Iterable<IGlobalLootModifier> getSortedModifiers() {
        return sortedModifiers;
    }
}
