/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = PlayerTests.GROUP + ".fakeplayer")
public class FakePlayerTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FakePlayer does not leak PlayerAdvancements entries in the cache (#1487)")
    static void fakePlayerAdvancementsDoNotLeak(final ExtendedGameTestHelper helper) {
        var level = helper.getLevel();
        int sizeBefore = getAdvancementsCacheSize(level.getServer().getPlayerList());

        for (int i = 0; i < 10; i++) {
            FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "Fake" + i));
        }

        int sizeAfter = getAdvancementsCacheSize(level.getServer().getPlayerList());

        helper.assertTrue(
                sizeAfter == sizeBefore,
                "PlayerList advancements cache grew from " + sizeBefore + " to " + sizeAfter + " after creating 10 fake players");

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
    private static int getAdvancementsCacheSize(PlayerList playerList) {
        try {
            Field field = PlayerList.class.getDeclaredField("advancements");
            field.setAccessible(true);
            return ((Map<UUID, PlayerAdvancements>) field.get(playerList)).size();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
