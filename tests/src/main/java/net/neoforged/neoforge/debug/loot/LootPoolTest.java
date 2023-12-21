/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.loot;

import java.util.List;
import java.util.Set;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "loot")
public class LootPoolTest {
    private static final ResourceLocation TEST_LOOT_TABLE = new ResourceLocation("neoforge", "test_loot_table");

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if loading loot pools with custom names works")
    public static void testPoolLoading(DynamicTest test, RegistrationHelper reg) {
        reg.addProvider(event -> new LootTableProvider(
                event.getGenerator().getPackOutput(),
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(() -> p_249643_ -> {
                            p_249643_.accept(
                                    TEST_LOOT_TABLE,
                                    LootTable.lootTable()
                                            .withPool(LootPool.lootPool()
                                                    .add(LootItem.lootTableItem(Items.DIAMOND))
                                                    .name("custom_name"))
                                            .withPool(LootPool.lootPool()
                                                    .add(LootItem.lootTableItem(Items.GOLD_NUGGET))));
                        }, LootContextParamSets.ALL_PARAMS))));

        test.onGameTest(helper -> {
            var testTable = helper.getLevel().getServer().getLootData().getLootTable(TEST_LOOT_TABLE);

            helper.assertTrue(testTable.getPool("custom_name") != null, "Expected custom_name pool");
            helper.assertTrue(testTable.getPool("pool1") != null, "Expected unnamed pool pool1");

            helper.succeed();
        });
    }
}
