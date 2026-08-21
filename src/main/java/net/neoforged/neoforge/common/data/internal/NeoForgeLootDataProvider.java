/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.loot.CanItemPerformAbility;

/**
 * Currently used only for replacing shears item to shears_dig item ability
 */
public final class NeoForgeLootDataProvider {
    public static void overrideLootPredicates(BootstrapContext<LootItemCondition> registry) {
        registry.register(LootPredicates.TOOL_CAN_SHEAR, CanItemPerformAbility.canItemPerformAbility(ItemAbilities.SHEARS_DIG).build());
    }

    private NeoForgeLootDataProvider() { }
}
