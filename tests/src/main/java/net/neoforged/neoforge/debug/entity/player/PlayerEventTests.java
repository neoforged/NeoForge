/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = { PlayerTests.GROUP + ".event", "event" })
public class PlayerEventTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the living swap items event is fired")
    static void playerNameEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final PlayerEvent.NameFormat event) -> {
            if (event.getEntity().getGameProfile().getName().equals("test-mock-player")) {
                event.setDisplayname(Component.literal("hello world"));
            }
            test.pass();
        });

        test.onGameTest(helper -> {
            helper.assertEntityProperty(
                    helper.makeMockPlayer(),
                    player -> player.getDisplayName().getString(),
                    "display name",
                    "hello world");
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the UseItemOnBlockEvent fires, cancelling item logic if dirt is placed on top of dispenser")
    static void useItemOnBlockEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final UseItemOnBlockEvent event) -> {
            UseOnContext context = event.getUseOnContext();
            Level level = context.getLevel();
            // cancel item logic if dirt is placed on top of dispenser
            if (event.getUsePhase() == UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
                ItemStack stack = context.getItemInHand();
                Item item = stack.getItem();
                if (item instanceof BlockItem blockItem && blockItem.getBlock() == Blocks.DIRT) {
                    BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
                    if (level.getBlockState(placePos.below()).getBlock() == Blocks.DISPENSER) {
                        if (!level.isClientSide) {
                            context.getPlayer().displayClientMessage(Component.literal("Can't place dirt on dispenser"), false);
                        }
                        test.pass();
                        event.cancelWithResult(InteractionResult.SUCCESS);
                    }
                }
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.DISPENSER))
                .thenExecute(() -> helper.useOn(
                        new BlockPos(1, 1, 1),
                        Items.DIRT.getDefaultInstance(),
                        helper.makeMockPlayer(),
                        Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.DIRT, 1, 2, 1))
                .thenSucceed());
    }
}
