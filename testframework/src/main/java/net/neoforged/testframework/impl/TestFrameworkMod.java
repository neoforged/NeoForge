/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.testframework.condition.TestEnabledLootCondition;

@Mod("testframework")
public class TestFrameworkMod {
    public static final DeferredRegister<LootItemConditionType> CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, "testframework");
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> TEST_ENABLED = CONDITIONS.register("test_enabled", () -> new LootItemConditionType(TestEnabledLootCondition.CODEC));

    public TestFrameworkMod(IEventBus bus) {
        CONDITIONS.register(bus);
    }
}
