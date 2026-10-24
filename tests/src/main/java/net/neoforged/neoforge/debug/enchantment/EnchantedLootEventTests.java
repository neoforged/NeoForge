/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.enchantment;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.enchanting.EnchantedBlockLootEvent;
import net.neoforged.neoforge.event.enchanting.EnchantedEntityLootEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jspecify.annotations.Nullable;

@ForEachTest(groups = "enchantment_loot")
public class EnchantedLootEventTests {
    /// evaluates the given loot table using the given seed
    private static List<ItemStack> getLootTableItems(ResourceKey<LootTable> lootTable, LootParams params, long seed) {
        return params.getLevel().getServer().reloadableRegistries().getLootTable(lootTable).getRandomItems(params, seed);
    }

    /// counts the instances of the item in the drops
    private static int countDrops(List<ItemStack> drops, Item item) {
        int count = 0;
        for (ItemStack stack : drops) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests whether EnchantedBlockLootEvent can use the entity context to increase fortune level.")
    static void enchantedBlockLootEvent(DynamicTest test, RegistrationHelper reg) {
        ResourceKey<LootTable> conditionLootTable = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(test.createModId(), "flat_chance"));

        reg.addClientProvider(event -> new LootTableProvider(
                event.getGenerator().getPackOutput(),
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(provider -> (consumer) -> {
                            consumer.accept(
                                    conditionLootTable,
                                    LootTable.lootTable().withPool(LootPool.lootPool()
                                            .add(LootItem.lootTableItem(Items.DIAMOND)
                                                    // 0% chance at level 0, 100% at level 1
                                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), 0, 1)))));
                        }, LootContextParamSets.BLOCK)),
                event.getLookupProvider()));

        test.eventListeners().forge().addListener((EnchantedBlockLootEvent e) -> {
            if (e.getEnchantment().is(Enchantments.FORTUNE) && e.getEntity() instanceof LivingEntity living && living.getOffhandItem().is(Items.LAPIS_LAZULI)) {
                // high enough to be hard to miss
                e.setEnchantmentLevel(25);
            }
        });
        test.onGameTest(helper -> {
            // to avoid the test randomly failing from bad luck on the lapis loot table, we choose a seed where the higher fortune level guarantees more drops
            // the important thing is that the drop count is impossible without that level of fortune
            long functionSeed = 1337;
            Player player = helper.makeMockPlayer();

            // lapis has a very obvious fortune impact
            Block block = Blocks.LAPIS_ORE;
            Optional<ResourceKey<LootTable>> functionLootTable = block.getLootTable();
            helper.assertTrue(functionLootTable.isPresent(), "Missing loot table for lapis lazuli");

            // build context, we can reuse it for all calls
            LootParams params = new LootParams.Builder(helper.getLevel())
                    .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, null)
                    .withParameter(LootContextParams.BLOCK_STATE, block.defaultBlockState())
                    .create(LootContextParamSets.BLOCK);

            // empty hand means no fortune
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);

            // should get 0 diamonds without fortune
            List<ItemStack> drops = getLootTableItems(conditionLootTable, params, 0);
            int count = countDrops(drops, Items.DIAMOND);
            helper.assertTrue(count == 0, "Received " + count + " diamonds without fortune from event, expected 0.");

            // lapis drops at most 9 items without fortune, verify we get drops in that range
            drops = getLootTableItems(functionLootTable.get(), params, functionSeed);
            count = countDrops(drops, Items.LAPIS_LAZULI);
            helper.assertTrue(count > 0, "Found no lapis lazuli in drops, this likely means the wrong loot table was queried.");
            helper.assertTrue(count <= 9, "Received " + count + " lapis lazuli, which is higher than the maximum 9 the lapis ore loot table produces without fortune.");

            // add fortune by putting lapis in the offhand
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.LAPIS_LAZULI));

            // should get exactly 1 diamond with fortune
            drops = getLootTableItems(conditionLootTable, params, 0);
            count = countDrops(drops, Items.DIAMOND);
            helper.assertTrue(count == 1, "Received " + count + " diamonds with fortune from event, expected 1.");

            // will get some number larger than the max without fortune for lapis drops
            drops = getLootTableItems(functionLootTable.get(), params, functionSeed);
            count = countDrops(drops, Items.LAPIS_LAZULI);
            helper.assertTrue(count > 9, "Received " + count + " lapis lazuli, despite the fact Fortune 25 should drop way more.");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests whether EnchantedEntityLootEvent can use the attacker entity and target entity context to increase looting level.")
    static void enchantedEntityLootEvent(DynamicTest test, RegistrationHelper reg) {
        ResourceKey<LootTable> conditionLootTable = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(test.createModId(), "flat_chance"));

        reg.addClientProvider(event -> new LootTableProvider(
                event.getGenerator().getPackOutput(),
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(provider -> (consumer) -> {
                            consumer.accept(
                                    conditionLootTable,
                                    LootTable.lootTable().withPool(LootPool.lootPool()
                                            .add(LootItem.lootTableItem(Items.DIAMOND)
                                                    // 0% chance at level 0, 100% at level 1
                                                    .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(provider, 0, 1)))));
                        }, LootContextParamSets.ENTITY)),
                event.getLookupProvider()));

        test.eventListeners().forge().addListener((EnchantedEntityLootEvent e) -> {
            if (e.getEnchantment().is(Enchantments.LOOTING)) {
                // chances are effectively boolean so 1 level is fine
                if (e.getAttacker() instanceof LivingEntity living && living.getOffhandItem().is(Items.LAPIS_LAZULI)) {
                    e.setEnchantmentLevel(25);
                }
                if (e.getEntity().getOffhandItem().is(Items.EMERALD)) {
                    e.setEnchantmentLevel(25);
                }
            }
        });
        test.onGameTest(helper -> {
            // to avoid the test randomly failing from bad luck on the zombie loot table, we choose a seed where the higher looting level guarantees more drops
            // the important thing is that the drop count is impossible without that level of looting
            long functionSeed = 1337;
            Player player = helper.makeMockPlayer();
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
            Optional<ResourceKey<LootTable>> functionLootTable = zombie.getLootTable();
            helper.assertTrue(functionLootTable.isPresent(), "Missing loot table for zombie");

            // run for both a player attacker and a null attacker ot make sure the event fires without a living attacker
            for (Player attacker : new @Nullable Player[] { player, null }) {
                String variant = attacker != null ? "with attacker" : "without attacker";
                // build context, we can reuse it for all calls
                DamageSources damageSources = helper.getLevel().damageSources();
                LootParams params = new LootParams.Builder(helper.getLevel())
                        .withParameter(LootContextParams.THIS_ENTITY, zombie)
                        .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                        .withParameter(LootContextParams.DAMAGE_SOURCE, attacker != null ? damageSources.playerAttack(attacker) : damageSources.fall())
                        .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, attacker)
                        .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, attacker)
                        .create(LootContextParamSets.ENTITY);

                // no looting with empty offhand
                zombie.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                // should get 0 diamonds in the condition table
                List<ItemStack> drops = getLootTableItems(conditionLootTable, params, 0);
                int count = countDrops(drops, Items.DIAMOND);
                helper.assertTrue(count == 0, "Received " + count + " diamonds without looting " + variant + ", expected 0.");
                // should get at most 2 rotten flesh in the function table
                drops = getLootTableItems(conditionLootTable, params, 0);
                count = countDrops(drops, Items.ROTTEN_FLESH);
                helper.assertTrue(count <= 2, "Received " + count + " rotten flesh without looting " + variant + ", which is higher than the maximum 2 the zombie loot table produces.");

                // add fortune by target holding emerald in offhand
                zombie.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.EMERALD));
                // condition table will get exactly 1 diamond
                drops = getLootTableItems(conditionLootTable, params, 0);
                count = countDrops(drops, Items.DIAMOND);
                helper.assertTrue(count == 1, "Received " + count + " diamonds with looting from target " + variant + ", expected 1.");
                // will get some number larger than the max without fortune for lapis drops
                drops = getLootTableItems(functionLootTable.get(), params, functionSeed);
                count = countDrops(drops, Items.ROTTEN_FLESH);
                helper.assertTrue(count > 2, "Received " + count + " rotten flesh with looting from target " + variant + ", despite the fact Looting 25 should drop way more.");

                // alternatively add fortune by attacker holding lapis - same deal as above
                if (attacker != null) {
                    zombie.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                    attacker.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.LAPIS_LAZULI));
                    drops = getLootTableItems(conditionLootTable, params, 0);
                    count = countDrops(drops, Items.DIAMOND);
                    helper.assertTrue(count == 1, "Received " + count + " diamonds with looting from attacker, expected 1.");
                    drops = getLootTableItems(functionLootTable.get(), params, functionSeed);
                    count = countDrops(drops, Items.ROTTEN_FLESH);
                    helper.assertTrue(count > 2, "Received " + count + " rotten flesh with looting from attacker, despite the fact Looting 25 should drop way more.");
                }
            }

            zombie.remove(Entity.RemovalReason.DISCARDED);
            helper.succeed();
        });
    }
}
