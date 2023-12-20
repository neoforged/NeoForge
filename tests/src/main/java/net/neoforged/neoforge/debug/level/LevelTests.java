/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.level;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = LevelTests.GROUP)
public class LevelTests {
    public static final String GROUP = "level";

    /**
     * Simple test to ensure custom game rules can be registered correctly and used in game.
     * <p>
     * To test these game rules use the following commands.
     * <br>If the game rules are registered correctly, they should show up as auto-completion values and be able to be changed to valid values based on their types.
     * <br>These game rules should also show up and be editable under the {@code Edit Game Rules} screen, when creating a new world.
     * <br>{@code Create new world > More (tab) > Game Rules > Misc}
     * <ul>
     * <li>
     *
     * <pre>{@code /gamerule neotests_custom_game_rule:custom_boolean_game_rule <true|false>}</pre>
     *
     * </li>
     * Should be able to be set to either {@code true} or {@code false} (Defaulting to {@code true}).
     *
     * <li>
     *
     * <pre>{@code /gamerule neotests_custom_game_rule:custom_integer_game_rule <some integer>}</pre>
     *
     * </li>
     * Should be able to be set to any integer value (Defaulting to {@code 1337}).
     * </ul>
     */
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom game rules work")
    static void customGameRule(final DynamicTest test) {
        final GameRules.Key<GameRules.BooleanValue> booleanGameRule = GameRules.register("%s:custom_boolean_game_rule".formatted(test.createModId()), GameRules.Category.MISC, GameRules.BooleanValue.create(true));
        final GameRules.Key<GameRules.IntegerValue> integerGameRule = GameRules.register("%s:custom_integer_game_rule".formatted(test.createModId()), GameRules.Category.MISC, GameRules.IntegerValue.create(1337));

        test.eventListeners().forge().addListener((final LivingEvent.LivingTickEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player && player.getGameProfile().getName().equals("test-mock-player")) {
                if (player.level().getGameRules().getBoolean(booleanGameRule)) {
                    player.setHealth(player.getHealth() - player.level().getGameRules().getInt(integerGameRule));
                }
            }
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);

            final var boolRule = player.level().getGameRules().getRule(booleanGameRule);
            final var intRule = player.level().getGameRules().getRule(integerGameRule);

            final var oldBool = boolRule.get();
            final var oldInt = intRule.get();

            helper.startSequence()
                    .thenExecute(() -> boolRule.set(true, player.server))
                    .thenExecute(() -> intRule.set(12, player.server))

                    .thenIdle(1)
                    .thenExecute(() -> helper.assertEntityProperty(player, ServerPlayer::getHealth, "player health", 8f))

                    .thenExecute(() -> boolRule.set(oldBool, player.server))
                    .thenExecute(() -> intRule.set(oldInt, player.server))
                    .thenSucceed();
        });
    }
}
