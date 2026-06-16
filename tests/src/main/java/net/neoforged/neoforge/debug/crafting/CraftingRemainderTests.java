/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "crafting_remainder_tests")
public interface CraftingRemainderTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures lava buckets turn into empty buckets when used as fuel", enabledByDefault = true)
    static void testLavaBucketToEmptyBucket(DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(BlockPos.ZERO, Blocks.FURNACE))
                .thenMap(() -> helper.getBlockEntity(BlockPos.ZERO, FurnaceBlockEntity.class))
                .thenExecute(furnace -> {
                    furnace.setItem(/* AbstractFurnaceBlockEntity.SLOT_INPUT */ 0, new ItemStack(Items.RAW_IRON, 64));
                    furnace.setItem(/* AbstractFurnaceBlockEntity.SLOT_FUEL */ 1, new ItemStack(Items.LAVA_BUCKET));
                })
                .thenIdle(1)
                .thenMap(furnace -> furnace.getItem(/* AbstractFurnaceBlockEntity.SLOT_FUEL */ 1))
                .thenExecute(fuel -> {
                    if (!fuel.is(Items.BUCKET)) {
                        helper.fail("Exepected crafting raminder to be '" + itemName(Items.BUCKET) + "' but found '" + itemName(fuel.getItem()) + "'");
                    }
                })
                .thenSucceed());
    }

    private static String itemName(Item item) {
        return item.builtInRegistryHolder().getRegisteredName();
    }
}
