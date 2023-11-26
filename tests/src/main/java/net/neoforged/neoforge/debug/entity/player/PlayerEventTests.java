package net.neoforged.neoforge.debug.entity.player;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = {PlayerTests.GROUP + ".event", "event"})
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
                    "hello world"
            );
            helper.succeed();
        });
    }
}
