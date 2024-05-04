/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.Hopper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemAllowPickupEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = ItemEntityTests.GROUP)
public class ItemEntityTests {
    public static final String GROUP = "level.entity.item";

    static {
        NeoForge.EVENT_BUS.addListener(ItemAllowPickupEvent.class, event -> {
            if (event.getEntity().getItem().is(Items.DIAMOND))
                event.setCanceled(true);
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if players can not pickup items via cancelled ItemAllowPickupEvent")
    static void itemPickupsPlayerTest(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(centerBlock(helper).below(), Blocks.BARRIER))
                .thenMap(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL).moveToCentre())
                .thenExecute(() -> helper.spawnItem(Items.DIAMOND, centerBlock(helper)))
                .thenExecuteAfter(SharedConstants.TICKS_PER_SECOND, () -> helper.assertItemEntityPresent(Items.DIAMOND))
                .thenExecute(player -> helper.assertTrue(!player.getInventory().hasAnyMatching(stack -> stack.is(Items.DIAMOND)), "Player picked up Diamond when they should not have"))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if mobs can not pickup items via cancelled ItemAllowPickupEvent")
    static void itemPickupsMobTest(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(centerBlock(helper).below(), Blocks.BARRIER))
                .thenMap(() -> {
                    var zombie = helper.spawn(EntityType.ZOMBIE, centerBlock(helper));
                    // zombies spawn with random pickup chances (higher on hard difficulty)
                    // this ensures the zombie can pickup items
                    zombie.setCanPickUpLoot(true);
                    return zombie;
                })
                .thenExecute(() -> helper.spawnItem(Items.DIAMOND, centerBlock(helper)))
                .thenExecuteAfter(SharedConstants.TICKS_PER_SECOND, () -> helper.assertItemEntityPresent(Items.DIAMOND))
                .thenExecute(zombie -> {
                    var hasDiamond = false;

                    for (var hand : zombie.getHandSlots()) {
                        if (hand.is(Items.DIAMOND)) {
                            hasDiamond = true;
                            break;
                        }
                    }

                    helper.assertTrue(!hasDiamond, "Zombie picked up Diamond when they should not have");
                })
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if minecart hoppers can not pickup items via cancelled ItemAllowPickupEvent")
    static void itemPickupsHopperMinecartTest(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(centerBlock(helper).below(), Blocks.BARRIER))
                .thenMap(() -> helper.spawn(EntityType.HOPPER_MINECART, centerBlock(helper)))
                .thenExecute(() -> helper.spawnItem(Items.DIAMOND, centerBlock(helper)))
                .thenExecuteAfter(SharedConstants.TICKS_PER_SECOND, () -> helper.assertItemEntityPresent(Items.DIAMOND))
                .thenExecute(hopper -> helper.assertTrue(!hopper.hasAnyMatching(stack -> stack.is(Items.DIAMOND)), "Hopper (Minecart) picked up Diamond when it should not have"))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if block entity hoppers can not pickup items via cancelled ItemAllowPickupEvent")
    static void itemPickupsHopperBlockEntityTest(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(centerBlock(helper), Blocks.HOPPER))
                .thenExecute(() -> helper.spawnItem(Items.DIAMOND, centerBlock(helper).above()))
                .thenExecuteAfter(SharedConstants.TICKS_PER_SECOND, () -> helper.assertBlockPresent(Blocks.HOPPER, centerBlock(helper)))
                .thenExecute(() -> helper.assertItemEntityPresent(Items.DIAMOND))
                .thenExecute(() -> {
                    var blockEntity = helper.getBlockEntity(centerBlock(helper));

                    if (!(blockEntity instanceof Hopper hopper)) {
                        helper.fail("Expected HopperBlockEntity but found %s".formatted(blockEntity == null ? "<NULL>" : Util.getRegisteredName(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntity.getType())));
                        return;
                    }

                    helper.assertTrue(!hopper.hasAnyMatching(stack -> stack.is(Items.DIAMOND)), "Hopper (BlockEntity) picked up Diamond when it should not have");
                })
                .thenSucceed());
    }

    private static BlockPos centerBlock(ExtendedGameTestHelper helper) {
        var size = helper.testInfo.getStructureBlockEntity().getStructureSize();
        return new BlockPos(
                size.getX() / 2,
                helper.testInfo.getStructureName().endsWith("_floor") ? 2 : 1,
                size.getX() / 2);
    }
}
