package net.neoforged.neoforge.debug.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = {PlayerTests.GROUP + ".event.xp", "event"})
public class PlayerXpTests {
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the player pickup xp is fired")
    static void playerPickupXp(final DynamicTest test) {
        test.eventListeners().forge().addListener((final PlayerXpEvent.PickupXp event) -> {
            if (event.getEntity().experienceLevel >= 2) {
                event.setCanceled(true);
            }
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
            // Move the player to the centre
            player.moveTo(helper.absoluteVec(new BlockPos(1, 2, 1).getCenter().subtract(0, 0.5, 0)));

            helper.startSequence()
                    .thenExecuteFor(5, () -> helper.spawn(EntityType.EXPERIENCE_ORB, 1, 2, 1).value = 10)
                    .thenIdle(40)
                    // The player is only allowed 2 levels of xp, as any further progress will be cancelled in the event listener
                    .thenExecute(() -> helper.assertEntityProperty(player, p -> p.experienceLevel, "experience level", 2))
                    .thenIdle(10)
                    // The player collected 2 orbs, 3 orbs remain
                    .thenExecute(() -> helper.assertTrue(
                            helper.getEntities(EntityType.EXPERIENCE_ORB, new BlockPos(1, 1, 1), 1.5).size() == 3,
                            "Expected 3 orbs to remain"
                    ))
                    .thenSucceed();
        });
    }
}
