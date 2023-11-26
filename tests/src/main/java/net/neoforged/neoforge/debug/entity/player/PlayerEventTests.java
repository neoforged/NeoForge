package net.neoforged.neoforge.debug.entity.player;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.Objects;

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

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the advancement earn event is fired")
    static void playerAdvancementEarn(final DynamicTest test) {
        test.eventListeners().forge().addListener((final AdvancementEvent.AdvancementEarnEvent event) -> {
            if (event.getAdvancement().id().equals(new ResourceLocation("story/root")) && event.getEntity() instanceof ServerPlayer player) {
                player.getAdvancements().award(
                        Objects.requireNonNull(player.server.getAdvancements().get(new ResourceLocation("story/mine_stone"))),
                        "get_stone"
                );
            }
            test.pass();
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);
            helper.startSequence()
                    .thenExecute(() -> player.getInventory().add(Items.CRAFTING_TABLE.getDefaultInstance()))
                    .thenExecuteAfter(5, () -> helper.assertTrue(
                            player.getAdvancements().getOrStartProgress(player.server.getAdvancements().get(new ResourceLocation("story/mine_stone"))).isDone(),
                            "Player did not receive advancement"
                    ))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the advancement progress event is fired")
    static void playerAdvancementProgress(final DynamicTest test) {
        test.eventListeners().forge().addListener((final AdvancementEvent.AdvancementProgressEvent event) -> {
            if (event.getAdvancement().id().equals(new ResourceLocation("story/obtain_armor")) && event.getProgressType() == AdvancementEvent.AdvancementProgressEvent.ProgressType.GRANT && event.getEntity() instanceof ServerPlayer player) {
                player.getAdvancements().getOrStartProgress(event.getAdvancement())
                                .getRemainingCriteria().forEach(criteria -> player.getAdvancements().award(event.getAdvancement(), criteria));
            }
            test.pass();
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);
            helper.startSequence()
                    .thenExecute(() -> player.getInventory().add(Items.IRON_HELMET.getDefaultInstance()))
                    .thenExecuteAfter(5, () -> helper.assertTrue(
                            player.getAdvancements().getOrStartProgress(player.server.getAdvancements().get(new ResourceLocation("story/obtain_armor"))).isDone(),
                            "Player did not complete advancement"
                    ))
                    .thenSucceed();
        });
    }
}
