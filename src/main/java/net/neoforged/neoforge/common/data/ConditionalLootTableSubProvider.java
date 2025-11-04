/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Wrapper around a {@link LootTableSubProvider} that adds conditions to all loot tables generated in the wrapped provider.
 */
public record ConditionalLootTableSubProvider(LootTableSubProvider wrapped, List<ICondition> conditions) implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        wrapped.generate((key, builder) -> output.accept(key, builder.withConditions(conditions)));
    }
}
