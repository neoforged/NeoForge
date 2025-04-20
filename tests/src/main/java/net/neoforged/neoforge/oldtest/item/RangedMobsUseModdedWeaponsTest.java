/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(RangedMobsUseModdedWeaponsTest.MOD_ID)
public class RangedMobsUseModdedWeaponsTest {

    // Testing if the new alternative for ProjectileHelper.getWeaponHoldingHand works for vanilla mobs
    // as well as replacing their usages of LivingEntity#isHolding(Item) with LivingEntity#isHolding(Predicate<ItemStack>)
    // Skeletons and Illusioners should be able to use the modded bow.
    // Piglins and Pillagers should be able to use the modded crossbow.
    public static final boolean ENABLE = true;

    public static final String MOD_ID = "ranged_mobs_use_modded_weapons_test";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    private static final DeferredItem<Item> MODDED_BOW = ITEMS.registerItem("modded_bow", props -> new BowItem(props.durability(384)));
    private static final DeferredItem<Item> MODDED_CROSSBOW = ITEMS.registerItem("modded_crossbow", props -> new CrossbowItem(props.durability(326)));

    public RangedMobsUseModdedWeaponsTest(IEventBus modEventBus) {
        if (ENABLE) {
            ITEMS.register(modEventBus);
            modEventBus.addListener(this::addCreative);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(MODDED_BOW);
            event.accept(MODDED_CROSSBOW);
        }
    }
}
