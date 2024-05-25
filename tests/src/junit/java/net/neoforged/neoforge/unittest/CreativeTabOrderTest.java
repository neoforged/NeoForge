/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class CreativeTabOrderTest {
    public static final String MOD_ID = "creative_tab_order_test";
    private static final Set<TagKey<Item>> ENCHANTABLES = Set.of(
            ItemTags.FOOT_ARMOR_ENCHANTABLE,
            ItemTags.LEG_ARMOR_ENCHANTABLE,
            ItemTags.CHEST_ARMOR_ENCHANTABLE,
            ItemTags.HEAD_ARMOR_ENCHANTABLE,
            ItemTags.ARMOR_ENCHANTABLE,
            ItemTags.SWORD_ENCHANTABLE,
            ItemTags.SHARP_WEAPON_ENCHANTABLE,
            ItemTags.MACE_ENCHANTABLE,
            ItemTags.FIRE_ASPECT_ENCHANTABLE,
            ItemTags.WEAPON_ENCHANTABLE,
            ItemTags.MINING_ENCHANTABLE,
            ItemTags.MINING_LOOT_ENCHANTABLE,
            ItemTags.FISHING_ENCHANTABLE,
            ItemTags.TRIDENT_ENCHANTABLE,
            ItemTags.DURABILITY_ENCHANTABLE,
            ItemTags.BOW_ENCHANTABLE,
            ItemTags.EQUIPPABLE_ENCHANTABLE,
            ItemTags.CROSSBOW_ENCHANTABLE,
            ItemTags.VANISHING_ENCHANTABLE);
    public static Iterable<Map.Entry<ItemStack, CreativeModeTab.TabVisibility>> ingredientsTab;
    public static Iterable<Map.Entry<ItemStack, CreativeModeTab.TabVisibility>> searchTab;

    @BeforeAll
    static void testSetupTabs(MinecraftServer server) {
        CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, true, server.registryAccess());
    }

    /**
     * The local tabEnchantments variable comes from {@link CreativeModeTabs#generateEnchantmentBookTypesOnlyMaxLevel(CreativeModeTab.Output, HolderLookup, Set, CreativeModeTab.TabVisibility, FeatureFlagSet)}
     * 
     * @param server Ephemeral server from extension
     */
    @Test
    void testIngredientsEnchantmentExistence(MinecraftServer server) {
        final Set<ItemStack> tabEnchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .map(enchantment -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.value().getMaxLevel())))
                .collect(() -> new ObjectOpenCustomHashSet<>(ItemStackLinkedSet.TYPE_AND_TAG), ObjectOpenCustomHashSet::add, ObjectOpenCustomHashSet::addAll);
        for (Map.Entry<ItemStack, CreativeModeTab.TabVisibility> entry : ingredientsTab) {
            if (entry.getValue() == CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY) {
                continue;
            }
            if (entry.getKey().getItem() == Items.ENCHANTED_BOOK) {
                Assertions.assertTrue(tabEnchantments.remove(entry.getKey()), "Enchanted book present that does not exist in the default set?");
            }
        }

        Assertions.assertTrue(tabEnchantments.isEmpty(), "Missing enchantments in Ingredient tab.");
    }

    /**
     * The local tabEnchantments variable comes from {@link CreativeModeTabs#generateEnchantmentBookTypesAllLevels(CreativeModeTab.Output, HolderLookup, Set, CreativeModeTab.TabVisibility, FeatureFlagSet)}
     * 
     * @param server Ephemeral server from extension
     */
    @Test
    void testSearchEnchantmentOrder(MinecraftServer server) {
        final var tabEnchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .flatMap(
                        enchantment -> IntStream.rangeClosed(enchantment.value().getMinLevel(), enchantment.value().getMaxLevel())
                                .mapToObj(p_270006_ -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, p_270006_))))
                .collect(() -> new ObjectOpenCustomHashSet<>(ItemStackLinkedSet.TYPE_AND_TAG), ObjectOpenCustomHashSet::add, ObjectOpenCustomHashSet::addAll);

        Enchantment enchantment = null;
        int level = 0;
        for (Map.Entry<ItemStack, CreativeModeTab.TabVisibility> entry : searchTab) {
            if (entry.getKey().getItem() != Items.ENCHANTED_BOOK) {
                continue;
            }
            final var enchantmentEntry = entry.getKey().get(DataComponents.STORED_ENCHANTMENTS).entrySet().iterator().next();
            final var entryEnchantment = enchantmentEntry.getKey().value();
            final var entryEnchantmentLevel = enchantmentEntry.getIntValue();
            if (enchantment == null || enchantment != entryEnchantment) {
                enchantment = entryEnchantment;
                Assertions.assertFalse(entryEnchantmentLevel > enchantment.getMinLevel(), "Enchantment does not start at the minimum level");
            } else {
                Assertions.assertTrue(entryEnchantmentLevel > level);
            }
            Assertions.assertTrue(tabEnchantments.remove(entry.getKey()), "Enchanted book present that does not exist in the default set?");
            level = entryEnchantmentLevel;
        }

        Assertions.assertTrue(tabEnchantments.isEmpty(), "Missing enchantments in Search tab.");
    }

    @Mod(MOD_ID)
    public static class CreativeTabOrderTestMod {
        public CreativeTabOrderTestMod(IEventBus modBus) {
            modBus.addListener(this::buildCreativeTab);
        }

        private void buildCreativeTab(final BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                ingredientsTab = event.getEntries();
            }
            if (event.getTabKey() == CreativeModeTabs.SEARCH) {
                searchTab = event.getEntries();
            }
        }
    }
}
