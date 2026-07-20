/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.server.PlayerAdvancements;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@ForEachTest(groups = PlayerTests.GROUP + ".fakeplayer")
public class FakePlayerTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FakePlayer does not leak CriteriaTrigger listener entries (#1487)")
    static void fakePlayerAdvancementsDoNotLeak(final ExtendedGameTestHelper helper) {
        var trigger = CriteriaTriggers.TICK;
        int sizeBefore = getListenerCount(trigger);

        var level = helper.getLevel();
        for (int i = 0; i < 10; i++) {
            FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "Fake" + i));
        }

        int sizeAfter = getListenerCount(trigger);

        helper.assertTrue(
                sizeAfter == sizeBefore,
                "CriteriaTrigger listener count grew from " + sizeBefore + " to " + sizeAfter + " after creating 10 fake players");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FakePlayer gets FakePlayerAdvancements even when sharing a UUID with a real player")
    static void fakePlayerGetsFakePlayerAdvancements(final ExtendedGameTestHelper helper) {
        var level = helper.getLevel();
        var realPlayer = helper.makeTickingMockServerPlayerInCorner(net.minecraft.world.level.GameType.SURVIVAL);

        var fakePlayer = FakePlayerFactory.get(level, realPlayer.getGameProfile());

        helper.assertTrue(
                fakePlayer.getAdvancements() instanceof FakePlayer.FakePlayerAdvancements,
                "FakePlayer should hold FakePlayerAdvancements");

        helper.succeed();
    }

    @SuppressWarnings("unchecked")
    private static int getListenerCount(SimpleCriterionTrigger<?> trigger) {
        try {
            Field field = SimpleCriterionTrigger.class.getDeclaredField("players");
            field.setAccessible(true);
            return ((Map<PlayerAdvancements, ?>) field.get(trigger)).size();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
