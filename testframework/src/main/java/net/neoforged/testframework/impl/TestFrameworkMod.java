/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.condition.TestEnabledIngredient;
import net.neoforged.testframework.condition.TestEnabledLootCondition;

@Mod("testframework")
public class TestFrameworkMod {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, "testframework");
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> TEST_ENABLED = LOOT_CONDITIONS.register("test_enabled", () -> new LootItemConditionType(TestEnabledLootCondition.CODEC));

    public static final DeferredRegister<IngredientType<?>> INGREDIENTS = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, "testframework");
    public static final DeferredHolder<IngredientType<?>, IngredientType<TestEnabledIngredient>> TEST_ENABLED_INGREDIENT = INGREDIENTS.register("test_enabled", () -> new IngredientType<>(TestEnabledIngredient.CODEC, TestEnabledIngredient.CODEC_NONEMPTY));

    public TestFrameworkMod(IEventBus bus) {
        LOOT_CONDITIONS.register(bus);
        INGREDIENTS.register(bus);
    }
}
