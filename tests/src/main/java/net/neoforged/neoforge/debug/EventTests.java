package net.neoforged.neoforge.debug;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.function.Consumer;

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
                        ItemStack::isSameItem
                ))
                .thenSucceed());
    }
}
