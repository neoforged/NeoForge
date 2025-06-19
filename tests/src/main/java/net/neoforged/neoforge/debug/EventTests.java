/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = EventTests.GROUP)
public class EventTests {
    public static final String GROUP = "event";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the datapack sync event works, by giving each player a fence on login")
    static void datapackSyncEvent(final DynamicTest test) {
        final Consumer<ServerPlayer> logger = player -> test.framework().logger().info("Sending modded datapack data to {}", player.getName().getString());
        test.eventListeners().forge().addListener((final OnDatapackSyncEvent event) -> {
            // Fired for a specific player on login
            if (event.getPlayer() != null) {
                logger.accept(event.getPlayer());
                event.getPlayer().addItem(Items.ACACIA_FENCE.getDefaultInstance());
            } else {
                // Fire for all players on /reload
                event.getPlayerList().getPlayers().forEach(logger);
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenIdle(5)
                .thenExecute(player -> helper.assertEntityProperty(
                        player,
                        p -> p.getInventory().getItem(0),
                        "item at index 0",
                        Items.ACACIA_FENCE.getDefaultInstance(),
                        ItemStack::isSameItem))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder
    static void alwaysFail(GameTestHelper helper) {
        // The line below shall always fail
        helper.fail(Component.literal("For testing... always fail"));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder
    static void alwaysFail2(ExtendedGameTestHelper helper) {
        helper.startSequence(() -> helper.makeMockPlayer(GameType.SPECTATOR))
                .thenExecute(player -> {
                    // The player should have health, so this also fails
                    helper.assertEntityProperty(player, LivingEntity::getHealth, "health", 0);
                });
    }
}
