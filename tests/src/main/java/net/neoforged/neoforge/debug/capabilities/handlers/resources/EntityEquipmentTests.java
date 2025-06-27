/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.resources;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.vanilla.")
public class EntityEquipmentTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests horse equipment")
    public static void testHorse(ExtendedGameTestHelper helper) {
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests Zombie equipment")
    public static void testZombie(ExtendedGameTestHelper helper) {
        var pos = new BlockPos(0, 0, 0);
        //Steve will receive armor (and attempt to wear a saddle) while Jim is left without armor.
        //This is to ensure that each zombie has its own handler for its equipment
        var zombieSteve = helper.spawn(EntityType.ZOMBIE, pos);
        var zombieJim = helper.spawn(EntityType.ZOMBIE, pos);

        //We have to tick them at least once make sure they can equip
        // the items since they are prevented on the first tick of their existence.
        zombieSteve.baseTick();
        zombieSteve.baseTick();
        zombieJim.baseTick();
        zombieJim.baseTick();

        var handler = helper.requireNotNull(zombieSteve.getCapability(Capabilities.ItemHandler.ENTITY), "Zombie must have a handler");
        var saddleResource = ItemResource.of(Items.SADDLE);
        var diamondChestResource = ItemResource.of(Items.DIAMOND_CHESTPLATE);

        try (var transaction = TransactionManager.open(null)) {
            var inserted = handler.insert(saddleResource, 1, transaction);
            var t = 2;
        }
        try (var transaction = TransactionManager.open(null)) {
            handler.insert(saddleResource, 1, transaction);
            transaction.commit();
            var t = 2;
        }

        try (var transaction = TransactionManager.open(null)) {
            handler.insert(diamondChestResource, 1, transaction);
            var t = 2;
        }
        try (var transaction = TransactionManager.open(null)) {
            handler.insert(diamondChestResource, 1, transaction);
            transaction.commit();
            var t = 2;
        }
        var saddle = zombieSteve.getItemBySlot(EquipmentSlot.SADDLE);
        var armor = zombieSteve.getItemBySlot(EquipmentSlot.CHEST);

        helper.assertFalse(saddleResource.is(saddle), "Steve not have a saddle");
        helper.assertTrue(diamondChestResource.is(armor), "Steve should have armor");

        helper.assertTrue(ItemStack.matches(zombieSteve.getItemBySlot(EquipmentSlot.SADDLE), zombieJim.getItemBySlot(EquipmentSlot.SADDLE)), "Neither should have a saddle.");
        helper.assertFalse(ItemStack.matches(zombieSteve.getItemBySlot(EquipmentSlot.CHEST), zombieJim.getItemBySlot(EquipmentSlot.CHEST)), "Only Jim should be wearing a chest piece");

        helper.succeed();
    }
}
