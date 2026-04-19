/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

public class LootModifierManager extends SimpleJsonResourceReloadListener<Optional<WithConditions<IGlobalLootModifier>>> {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final String FOLDER = "loot_modifiers";

    private BiMap<Identifier, IGlobalLootModifier> registeredLootModifiers = ImmutableBiMap.of();
    private List<IGlobalLootModifier> sortedModifiers = List.of();

    public LootModifierManager(RegistryAccess registries) {
        super(registries, IGlobalLootModifier.CONDITIONAL_CODEC, ResourceKey.createRegistryKey(Identifier.withDefaultNamespace(FOLDER)));
    }

    @Override
    protected void apply(Map<Identifier, Optional<WithConditions<IGlobalLootModifier>>> resourceList, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        Builder<Identifier, IGlobalLootModifier> builder = ImmutableBiMap.builder();
        for (Map.Entry<Identifier, Optional<WithConditions<IGlobalLootModifier>>> entry : resourceList.entrySet()) {
            if (entry.getValue().isPresent()) {
                builder.put(entry.getKey(), entry.getValue().get().carrier());
            }
        }

        this.registeredLootModifiers = builder.build();
        this.sortedModifiers = this.registeredLootModifiers.values().stream()
                .sorted(Comparator.comparingInt(glm -> -glm.priority())) // Use negative priority so higher priority executes first.
                .toList();
    }

    /**
     * Returns an iterable view of all loot modifiers, sorted in the order they should be applied.
     */
    public Iterable<IGlobalLootModifier> getSortedModifiers() {
        return sortedModifiers;
    }

    /**
     * Returns the ID of the given loot modifier, or null if it is not registered.
     */
    @Nullable
    public Identifier getId(IGlobalLootModifier modifier) {
        return this.registeredLootModifiers.inverse().get(modifier);
    }
}
