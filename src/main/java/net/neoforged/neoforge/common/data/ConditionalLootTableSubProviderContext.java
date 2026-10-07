/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.conditions.ICondition;

/// Wrapper around a [LootTableSubProvider.Context] that adds conditions to all loot tables registered to the wrapped context.
public record ConditionalLootTableSubProviderContext(LootTableSubProvider.Context wrapped, List<ICondition> conditions) implements LootTableSubProvider.Context {
    @Override
    public Holder.Reference<LootTable> accept(ResourceKey<LootTable> key, LootTable.Builder value) {
        return wrapped.accept(key, value.withConditions(conditions));
    }

    @Override
    public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
        return wrapped.lookup(key);
    }

    @Override
    @Deprecated
    public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
        return wrapped.listContextElements(key);
    }
}
