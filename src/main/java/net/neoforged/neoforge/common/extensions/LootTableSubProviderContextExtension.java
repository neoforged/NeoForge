/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.List;
import net.minecraft.data.loot.LootTableSubProvider;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.data.ConditionalLootTableSubProviderContext;

public interface LootTableSubProviderContextExtension {
    /// Builds a wrapper around this context that adds the provided conditions to all loot tables registered to this context.
    default LootTableSubProvider.Context withConditions(List<ICondition> conditions) {
        return conditions.isEmpty() ? self() : new ConditionalLootTableSubProviderContext(self(), conditions);
    }

    private LootTableSubProvider.Context self() {
        return (LootTableSubProvider.Context) this;
    }
}
