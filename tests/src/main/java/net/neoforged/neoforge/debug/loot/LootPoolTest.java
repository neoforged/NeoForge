/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.loot;

import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "loot")
public class LootPoolTest {
    private static final ResourceKey<LootTable> TEST_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, new ResourceLocation("neoforge", "test_loot_table"));

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if loading loot pools with custom names works")
    public static void testPoolLoading(DynamicTest test, RegistrationHelper reg) {
        reg.addProvider(event -> new LootTableProvider(
                event.getGenerator().getPackOutput(),
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(() -> (provider, consumer) -> {
                            consumer.accept(
                                    TEST_LOOT_TABLE,
                                    LootTable.lootTable()
                                            .withPool(LootPool.lootPool()
                                                    .add(LootItem.lootTableItem(Items.DIAMOND))
                                                    .name("custom_name"))
                                            .withPool(LootPool.lootPool()
                                                    .add(LootItem.lootTableItem(Items.GOLD_NUGGET))));
                        }, LootContextParamSets.ALL_PARAMS)),
                event.getLookupProvider()));

        test.onGameTest(helper -> {
            var testTable = helper.getLevel().getServer().reloadableRegistries().getLootTable(TEST_LOOT_TABLE);

            helper.assertTrue(testTable.getPool("custom_name") != null, "Expected custom_name pool");
            helper.assertTrue(testTable.getPool("pool1") != null, "Expected unnamed pool pool1");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the LootTableLoadEvent can cancel a Loot Table")
    static void pinkConcreteLootTableCanceled(final DynamicTest test) {
        ResourceLocation poolToCancel = new ResourceLocation("minecraft", "blocks/pink_concrete");
        test.eventListeners().forge().addListener((final LootTableLoadEvent event) -> {
            if (event.getName().equals(poolToCancel)) {
                event.setCanceled(true);
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> {
                    LootTable lootTable = helper.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, poolToCancel));
                    LootParams.Builder lootParamsBuilder = new LootParams.Builder(helper.getLevel())
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, null);
                    LootParams lootparams = lootParamsBuilder.withParameter(LootContextParams.BLOCK_STATE, Blocks.PINK_CONCRETE.defaultBlockState()).create(LootContextParamSets.BLOCK);
                    ObjectArrayList<ItemStack> collectedItems = lootTable.getRandomItems(lootparams);
                    helper.assertTrue(collectedItems.isEmpty(), "Pink Concrete Loot Table should be canceled and empty");
                })
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the LootTableLoadEvent can replace a Loot Table with another")
    static void orangeConcreteLootTableReplaced(final DynamicTest test) {
//        ResourceLocation poolToReplace = new ResourceLocation("minecraft", "blocks/orange_concrete");
//        ResourceLocation poolReplacement = new ResourceLocation("minecraft", "blocks/blue_concrete");
//        test.eventListeners().forge().addListener((final LootTableLoadEvent event) -> {
//            if (event.getName().equals(poolToReplace)) {
//                event.setTable();
//            }
//        });
//
//        test.onGameTest(helper -> helper.startSequence()
//                .thenExecute(() -> {
//                    LootTable lootTable = helper.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, poolToReplace));
//                    LootParams.Builder lootParamsBuilder = new LootParams.Builder(helper.getLevel())
//                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
//                            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
//                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, null);
//                    LootParams lootparams = lootParamsBuilder.withParameter(LootContextParams.BLOCK_STATE, Blocks.PINK_CONCRETE.defaultBlockState()).create(LootContextParamSets.BLOCK);
//                    ObjectArrayList<ItemStack> collectedItems = lootTable.getRandomItems(lootparams);
//                    helper.assertTrue(collectedItems.isEmpty(), "Pink Concrete Loot Table should be canceled and empty");
//                })
//                .thenSucceed());
    }
}
