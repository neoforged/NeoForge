/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
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
    private static final ResourceKey<CreativeModeTab> STONE_ORDERING = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(MOD_ID, "stone_ordering"));
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
    public static ObjectSortedSet<ItemStack> ingredientsTab;
    public static ObjectSortedSet<ItemStack> searchTab;
    public static ObjectSortedSet<ItemStack> stoneParentTab;
    public static ObjectSortedSet<ItemStack> stoneSearchTab;
    public static boolean stackCountExceptionForAccept = false;
    public static boolean stackCountExceptionForInsertAfter = false;
    public static boolean stackCountExceptionForInsertBefore = false;
    public static boolean stackCountExceptionForInsertFirst = false;
    public static boolean targetDoesNotExistExceptionForInsertAfter = false;
    public static boolean targetDoesNotExistExceptionForInsertBefore = false;
    public static boolean newEntryExistAlreadyExceptionForAccept = false;
    public static boolean newEntryExistAlreadyExceptionForInsertFirst = false;
    public static boolean newEntryExistAlreadyExceptionForInsertAfter = false;
    public static boolean newEntryExistAlreadyExceptionForInsertBefore = false;

    @BeforeAll
    static void testSetupTabs(MinecraftServer server) {
        CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, true, server.registryAccess());
    }

    /**
     * The local tabEnchantments variable comes from {@link CreativeModeTabs#generateEnchantmentBookTypesOnlyMaxLevel}
     * 
     * @param server Ephemeral server from extension
     */
    @Test
    void testIngredientsEnchantmentExistence(MinecraftServer server) {
        final Set<ItemStack> tabEnchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .map(enchantment -> EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, enchantment.value().getMaxLevel())))
                .collect(() -> new ObjectOpenCustomHashSet<>(ItemStackLinkedSet.TYPE_AND_TAG), ObjectOpenCustomHashSet::add, ObjectOpenCustomHashSet::addAll);
        for (ItemStack entry : ingredientsTab) {
            if (entry.is(Items.ENCHANTED_BOOK)) {
                Assertions.assertTrue(tabEnchantments.remove(entry), "Enchanted book present that does not exist in the default set?");
            }
        }

        Assertions.assertTrue(tabEnchantments.isEmpty(), "Missing enchantments in Ingredient tab.");
    }

    /**
     * The local tabEnchantments variable comes from {@link CreativeModeTabs#generateEnchantmentBookTypesAllLevels}
     * 
     * @param server Ephemeral server from extension
     */
    @Test
    void testSearchEnchantmentOrder(MinecraftServer server) {
        final var tabEnchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .flatMap(
                        enchantment -> IntStream.rangeClosed(enchantment.value().getMinLevel(), enchantment.value().getMaxLevel())
                                .mapToObj(p_270006_ -> EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, p_270006_))))
                .collect(() -> new ObjectOpenCustomHashSet<>(ItemStackLinkedSet.TYPE_AND_TAG), ObjectOpenCustomHashSet::add, ObjectOpenCustomHashSet::addAll);

        Enchantment enchantment = null;
        int level = 0;
        for (ItemStack entry : searchTab) {
            if (!entry.is(Items.ENCHANTED_BOOK)) {
                continue;
            }
            final var enchantmentEntry = entry.get(DataComponents.STORED_ENCHANTMENTS).entrySet().iterator().next();
            final var entryEnchantment = enchantmentEntry.getKey().value();
            final var entryEnchantmentLevel = enchantmentEntry.getIntValue();
            if (enchantment == null || enchantment != entryEnchantment) {
                enchantment = entryEnchantment;
                Assertions.assertFalse(entryEnchantmentLevel > enchantment.getMinLevel(), "Enchantment does not start at the minimum level");
            } else {
                Assertions.assertTrue(entryEnchantmentLevel > level);
            }
            Assertions.assertTrue(tabEnchantments.remove(entry), "Enchanted book present that does not exist in the default set?");
            level = entryEnchantmentLevel;
        }

        Assertions.assertTrue(tabEnchantments.isEmpty(), "Missing enchantments in Search tab.");
    }

    /**
     * Verifies that the tab sorting works properly where people can specify what item appears after what.
     *
     * @param server Ephemeral server from extension
     */
    @Test
    void testParentStoneOrder(MinecraftServer server) {
        List<Item> desiredOrder = setupDesiredStoneOrder();
        for (ItemStack entry : stoneParentTab) {
            Item currentDesiredItem = desiredOrder.removeFirst();
            Assertions.assertTrue(entry.is(currentDesiredItem), entry.getItem() + " is not the desired " + currentDesiredItem + " in the stone parent tab!");
        }
        Assertions.assertTrue(desiredOrder.isEmpty(), "Not all sorted stones were found in stone parent tab!");

        desiredOrder = setupDesiredStoneOrder();
        for (ItemStack entry : stoneSearchTab) {
            Item currentDesiredItem = desiredOrder.removeFirst();
            Assertions.assertTrue(entry.is(currentDesiredItem), entry.getItem() + " is not the desired " + currentDesiredItem + " in the stone search tab!");
        }
        Assertions.assertTrue(desiredOrder.isEmpty(), "Not all sorted stones were found in stone search tab!");
    }

    /**
     * Makes sure the validation checks were triggered properly for problematic inputs into {@link BuildCreativeModeTabContentsEvent}
     *
     * @param server Ephemeral server from extension
     */
    @Test
    void testBuildCreativeModeTabContentsEventValidations(MinecraftServer server) {
        Assertions.assertTrue(stackCountExceptionForAccept, "Accept method is missing itemstack validation where stack should be 1.");
        Assertions.assertTrue(stackCountExceptionForInsertAfter, "Insert After method is missing itemstack validation where stack should be 1.");
        Assertions.assertTrue(stackCountExceptionForInsertBefore, "Insert Before method is missing itemstack validation where stack should be 1.");
        Assertions.assertTrue(stackCountExceptionForInsertFirst, "Insert First method is missing itemstack validation where stack should be 1.");
        Assertions.assertTrue(targetDoesNotExistExceptionForInsertAfter, "Insert After method is missing target itemstack validation where target should exist.");
        Assertions.assertTrue(targetDoesNotExistExceptionForInsertBefore, "Insert Before method is missing target itemstack validation where target should exist.");
        Assertions.assertTrue(newEntryExistAlreadyExceptionForAccept, "Accept method is missing duplicate itemstack validation where entry should not be added twice.");
        Assertions.assertTrue(newEntryExistAlreadyExceptionForInsertFirst, "Insert First method is missing duplicate itemstack validation where entry should not be added twice.");
        Assertions.assertTrue(newEntryExistAlreadyExceptionForInsertAfter, "Insert After method is missing duplicate itemstack validation where entry should not be added twice.");
        Assertions.assertTrue(newEntryExistAlreadyExceptionForInsertBefore, "Insert Before method is missing duplicate itemstack validation where entry should not be added twice.");
    }

    private static List<Item> setupDesiredStoneOrder() {
        List<Item> desiredOrder = new ArrayList<>();
        desiredOrder.add(Items.BASALT);
        desiredOrder.add(Items.STONE);
        desiredOrder.add(Items.TUFF);
        desiredOrder.add(Items.GRANITE);
        desiredOrder.add(Items.DIORITE);
        desiredOrder.add(Items.BLACKSTONE);
        desiredOrder.add(Items.CALCITE);
        desiredOrder.add(Items.ANDESITE);
        return desiredOrder;
    }

    @Mod(MOD_ID)
    public static class CreativeTabOrderTestMod {
        public CreativeTabOrderTestMod(IEventBus modBus) {
            modBus.addListener(this::onCreativeModeTabRegister);
            modBus.addListener(this::buildCreativeTab);
        }

        private void buildCreativeTab(final BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == STONE_ORDERING) {
                var vis = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
                event.insertAfter(i(Blocks.STONE), i(Blocks.TUFF), vis);
                event.insertAfter(i(Blocks.DIORITE), i(Blocks.CALCITE), vis);
                event.insertBefore(i(Blocks.CALCITE), i(Blocks.BLACKSTONE), vis);
                event.accept(i(Blocks.CYAN_CONCRETE), vis);
                event.remove(i(Blocks.CYAN_CONCRETE), vis);
                event.insertFirst(i(Blocks.BASALT), vis);

                catchSpecificExceptionForAction(
                        () -> event.accept(new ItemStack(Items.DIRT, 4), vis),
                        "The stack count must be 1 for",
                        () -> stackCountExceptionForAccept = true);

                catchSpecificExceptionForAction(
                        () -> event.insertAfter(i(Blocks.STONE), new ItemStack(Items.DIRT, 4), vis),
                        "The stack count must be 1 for",
                        () -> stackCountExceptionForInsertAfter = true);

                catchSpecificExceptionForAction(
                        () -> event.insertBefore(i(Blocks.STONE), new ItemStack(Items.DIRT, 4), vis),
                        "The stack count must be 1 for",
                        () -> stackCountExceptionForInsertBefore = true);

                catchSpecificExceptionForAction(
                        () -> event.insertFirst(new ItemStack(Items.DIRT, 4), vis),
                        "The stack count must be 1 for",
                        () -> stackCountExceptionForInsertFirst = true);

                catchSpecificExceptionForAction(
                        () -> event.insertAfter(i(Blocks.LECTERN), i(Blocks.DIRT), vis),
                        "does not exist in tab's list",
                        () -> targetDoesNotExistExceptionForInsertAfter = true);

                catchSpecificExceptionForAction(
                        () -> event.insertBefore(i(Blocks.LECTERN), i(Blocks.DIRT), vis),
                        "does not exist in tab's list",
                        () -> targetDoesNotExistExceptionForInsertBefore = true);

                catchSpecificExceptionForAction(
                        () -> event.accept(i(Blocks.STONE), vis),
                        "already exists in the tab's list",
                        () -> newEntryExistAlreadyExceptionForAccept = true);

                catchSpecificExceptionForAction(
                        () -> event.insertFirst(i(Blocks.STONE), vis),
                        "already exists in the tab's list",
                        () -> newEntryExistAlreadyExceptionForInsertFirst = true);

                catchSpecificExceptionForAction(
                        () -> event.insertAfter(i(Blocks.DIORITE), i(Blocks.STONE), vis),
                        "already exists in the tab's list",
                        () -> newEntryExistAlreadyExceptionForInsertAfter = true);

                catchSpecificExceptionForAction(
                        () -> event.insertBefore(i(Blocks.DIORITE), i(Blocks.STONE), vis),
                        "already exists in the tab's list",
                        () -> newEntryExistAlreadyExceptionForInsertBefore = true);

                stoneParentTab = event.getParentEntries();
                stoneSearchTab = event.getSearchEntries();
            }

            if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                ingredientsTab = event.getParentEntries();
            }
            if (event.getTabKey() == CreativeModeTabs.SEARCH) {
                searchTab = event.getSearchEntries();
            }
        }

        private static void catchSpecificExceptionForAction(Runnable action, String targetExceptionMessage, Runnable foundExceptionAction) {
            try {
                action.run();
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains(targetExceptionMessage)) {
                    foundExceptionAction.run();
                }
            }
        }

        private void onCreativeModeTabRegister(RegisterEvent event) {
            event.register(Registries.CREATIVE_MODE_TAB, helper -> {
                helper.register(STONE_ORDERING, CreativeModeTab.builder().icon(() -> new ItemStack(Blocks.STONE))
                        .title(Component.literal("Stone Ordering"))
                        .withLabelColor(0x0000FF)
                        .displayItems((params, output) -> {
                            output.accept(new ItemStack(Blocks.STONE));
                            output.accept(new ItemStack(Blocks.GRANITE));
                            output.accept(new ItemStack(Blocks.DIORITE));
                            output.accept(new ItemStack(Blocks.ANDESITE));
                        })
                        .build());
            });
        }
    }

    private static ItemStack i(ItemLike item) {
        return new ItemStack(item);
    }
}
