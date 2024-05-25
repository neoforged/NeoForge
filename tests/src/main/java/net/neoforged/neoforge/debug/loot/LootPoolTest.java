/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.loot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "loot")
public class LootPoolTest {
    private static final ResourceKey<LootTable> TEST_LOOT_TABLE_1 = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("neoforge", "test_loot_table_1"));
    private static final ResourceKey<LootTable> TEST_LOOT_TABLE_2 = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("neoforge", "test_loot_table_2"));
    private static final ResourceKey<LootTable> TEST_LOOT_TABLE_3 = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("neoforge", "test_loot_table_3"));
    private static final ResourceKey<LootTable> TEST_LOOT_TABLE_4 = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("neoforge", "test_loot_table_4"));

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if loading loot pools with custom names works")
    public static void testPoolLoading(DynamicTest test, RegistrationHelper reg) {
        reg.addProvider(event -> new LootTableProvider(
                event.getGenerator().getPackOutput(),
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(provider -> (consumer) -> {
                            consumer.accept(
                                    TEST_LOOT_TABLE_1,
                                    LootTable.lootTable()
                                            .withPool(LootPool.lootPool()
                                                    .add(LootItem.lootTableItem(Items.DIAMOND))
                                                    .name("custom_name"))
                                            .withPool(LootPool.lootPool()
                                                    .add(LootItem.lootTableItem(Items.GOLD_NUGGET))));
                        }, LootContextParamSets.ALL_PARAMS)),
                event.getLookupProvider()));

        test.onGameTest(helper -> {
            var testTable = helper.getLevel().getServer().reloadableRegistries().getLootTable(TEST_LOOT_TABLE_1);

            helper.assertTrue(testTable.getPool("custom_name") != null, "Expected custom_name pool");
            helper.assertTrue(testTable.getPool("pool1") != null, "Expected unnamed pool pool1");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the LootTableLoadEvent can cancel a Loot Table")
    static void pinkConcreteLootTableCanceled(final DynamicTest test, final RegistrationHelper reg) {
        ResourceKey<LootTable> lootTableToUse = TEST_LOOT_TABLE_2;

        reg.addProvider(event -> new LootTableProvider(
                event.getGenerator().getPackOutput(),
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(provider -> consumer -> {
                            consumer.accept(
                                    lootTableToUse,
                                    LootTable.lootTable()
                                            .withPool(LootPool.lootPool()
                                                    .add(LootItem.lootTableItem(Items.PINK_CONCRETE))));
                        }, LootContextParamSets.ALL_PARAMS)),
                event.getLookupProvider()));

        NeoForge.EVENT_BUS.addListener((final LootTableLoadEvent event) -> {
            if (event.getName().equals(lootTableToUse.location())) {
                event.setCanceled(true);
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> {
                    LootTable lootTable = helper.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableToUse.location()));
                    LootParams.Builder lootParamsBuilder = new LootParams.Builder(helper.getLevel())
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, null);
                    LootParams lootparams = lootParamsBuilder.withParameter(LootContextParams.BLOCK_STATE, Blocks.PINK_CONCRETE.defaultBlockState()).create(LootContextParamSets.BLOCK);
                    ObjectArrayList<ItemStack> collectedItems = lootTable.getRandomItems(lootparams);
                    helper.assertTrue(collectedItems.isEmpty(), "neoforge:test_loot_table_2 Loot Table should be canceled and empty");
                })
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the LootTableLoadEvent can replace a Loot Table with another")
    static void orangeConcreteLootTableReplaced(final DynamicTest test, final RegistrationHelper reg) {
        ResourceKey<LootTable> lootTableToUse = TEST_LOOT_TABLE_3;

        reg.addProvider(event -> new LootTableProvider(
                event.getGenerator().getPackOutput(),
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(provider -> (consumer) -> {
                            consumer.accept(
                                    lootTableToUse,
                                    LootTable.lootTable()
                                            .withPool(LootPool.lootPool()
                                                    .add(LootItem.lootTableItem(Items.ORANGE_CONCRETE))));
                        }, LootContextParamSets.ALL_PARAMS)),
                event.getLookupProvider()));

        NeoForge.EVENT_BUS.addListener((final LootTableLoadEvent event) -> {
            if (event.getName().equals(lootTableToUse.location())) {
                LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(Items.BLUE_CONCRETE);
                LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(entry).when(ExplosionCondition.survivesExplosion());
                event.setTable(new LootTable.Builder().withPool(pool).build());
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> {
                    LootTable lootTable = helper.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableToUse.location()));
                    LootParams.Builder lootParamsBuilder = new LootParams.Builder(helper.getLevel())
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, null);
                    LootParams lootparams = lootParamsBuilder.withParameter(LootContextParams.BLOCK_STATE, Blocks.PINK_CONCRETE.defaultBlockState()).create(LootContextParamSets.BLOCK);
                    ObjectArrayList<ItemStack> collectedItems = lootTable.getRandomItems(lootparams);
                    helper.assertTrue(collectedItems.size() == 1 && collectedItems.get(0).getItem().equals(Items.BLUE_CONCRETE), "neoforge:test_loot_table_3 Loot Table should be replaced and drops Blue Concrete");
                })
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the LootTableLoadEvent can add a new pool to an existing loot table")
    static void yellowConcreteLootTableAppended(final DynamicTest test, final RegistrationHelper reg) {
        ResourceKey<LootTable> lootTableToUse = TEST_LOOT_TABLE_4;

        reg.addProvider(event -> new LootTableProvider(
                event.getGenerator().getPackOutput(),
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(provider -> consumer -> {
                            consumer.accept(
                                    lootTableToUse,
                                    LootTable.lootTable()
                                            .withPool(LootPool.lootPool()
                                                    .add(LootItem.lootTableItem(Items.YELLOW_CONCRETE))));
                        }, LootContextParamSets.ALL_PARAMS)),
                event.getLookupProvider()));

        NeoForge.EVENT_BUS.addListener((final LootTableLoadEvent event) -> {
            if (event.getName().equals(lootTableToUse.location())) {
                LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(Items.YELLOW_CONCRETE);
                LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(entry).when(ExplosionCondition.survivesExplosion());
                event.getTable().addPool(pool.build());
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> {
                    LootTable lootTable = helper.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableToUse.location()));
                    LootParams.Builder lootParamsBuilder = new LootParams.Builder(helper.getLevel())
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, null);
                    LootParams lootparams = lootParamsBuilder.withParameter(LootContextParams.BLOCK_STATE, Blocks.PINK_CONCRETE.defaultBlockState()).create(LootContextParamSets.BLOCK);
                    ObjectArrayList<ItemStack> collectedItems = lootTable.getRandomItems(lootparams);
                    helper.assertTrue(collectedItems.size() == 2 && collectedItems.stream().allMatch(itemStack -> itemStack.getItem().equals(Items.YELLOW_CONCRETE)), "neoforge:test_loot_table_4 Loot Table should drop 2 Yellow Concrete");
                })
                .thenSucceed());
    }
}
