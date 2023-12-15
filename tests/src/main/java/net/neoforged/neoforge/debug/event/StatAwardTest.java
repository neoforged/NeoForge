/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.event;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.StatAwardEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTestPlayer;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = StatAwardTest.GROUP)
public class StatAwardTest {
    public static final String GROUP = "events";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the StatsAwardEvent properly modifies stats stored per player")
    static void changeStatAward(final DynamicTest test, final RegistrationHelper reg) {
        test.eventListeners().forge().addListener(EventPriority.NORMAL, false, StatAwardEvent.class, event -> {
            //when damage is dealt, instead record this stat as a bell ring
            if (event.getStat().equals(Stats.CUSTOM.get(Stats.DAMAGE_TAKEN)))
                event.setStat(Stats.CUSTOM.get(Stats.BELL_RING));
        });
        test.eventListeners().forge().addListener(EventPriority.NORMAL, false, StatAwardEvent.class, event -> {
            //when awarded stats for breeding, multiply the value by 10
            if (event.getStat().equals(Stats.CUSTOM.get(Stats.ANIMALS_BRED)))
                event.setValue(event.getValue() * 10);
        });

        test.onGameTest(helper -> {
            GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
            //Award a damage stat, which we are listening for in order to change the stat
            player.awardStat(Stats.CUSTOM.get(Stats.DAMAGE_TAKEN), 100);
            //Award an animal breed stat, which we are listining for in order to multiply the value
            player.awardStat(Stats.CUSTOM.get(Stats.ANIMALS_BRED), 1);
            ServerStatsCounter stats = player.level().getServer().getPlayerList().getPlayerStats(player);
            //if our damage stat is changed to bell ring and our animal breed stat is multiplied by ten, the test passes
            if (stats.getValue(Stats.CUSTOM.get(Stats.BELL_RING)) == 100 && stats.getValue(Stats.CUSTOM.get(Stats.ANIMALS_BRED)) == 10)
                helper.succeed();
        });
    }
}
