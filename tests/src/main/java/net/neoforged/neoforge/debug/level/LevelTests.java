/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.level;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.event.RegisterGameRuleCategoryEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;

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
    static void customGameRule(final DynamicTest test, final RegistrationHelper reg) {
        final GameRuleCategory category = new GameRuleCategory(Identifier.fromNamespaceAndPath(reg.modId(), "game_rules"));
        final DeferredHolder<GameRule<?>, GameRule<Boolean>> booleanGameRule = reg.register(Registries.GAME_RULE, "custom_boolean_game_rule", (r, n) -> GameRules.registerBoolean(n.toString(), category, true));
        final DeferredHolder<GameRule<?>, GameRule<Integer>> integerGameRule = reg.register(Registries.GAME_RULE, "custom_integer_game_rule", (r, n) -> GameRules.registerInteger(n.toString(), category, 1337, 1337));
        // '(visitor, gameRule) -> {}' is intentional here
        // `GameRules#visitGameRuleTypes` calls the default `GameRuleTypeVisitor#visit(GameRule<T>)` before invoking the game rule specific visitor
        // DO NOT call `GameRuleTypeVisitor#visit` from your custom visitor, this will double up 'visits' to your game rule
        final DeferredHolder<GameRule<?>, GameRule<Double>> doubleGameRule = reg.register(Registries.GAME_RULE, "custom_double_game_rule", (r, n) -> GameRules.register(n.toString(), category, GameRuleType.valueOf("NEOTESTS_DOUBLE"), DoubleArgumentType.doubleArg(), Codec.DOUBLE, 0D, FeatureFlagSet.of(), (visitor, gameRule) -> {}, value -> Command.SINGLE_SUCCESS));
        final DeferredHolder<GameRule<?>, GameRule<String>> stringGameRule = reg.register(Registries.GAME_RULE, "custom_string_game_rule", (r, n) -> GameRules.register(n.toString(), category, GameRuleType.valueOf("NEOTESTS_STRING"), StringArgumentType.string(), Codec.STRING, "", FeatureFlagSet.of(), (visitor, gameRule) -> {}, value -> Command.SINGLE_SUCCESS));

        test.eventListeners().forge().addListener((EntityTickEvent.Pre event) -> {
            if (event.getEntity() instanceof ServerPlayer player && player.getGameProfile().name().equals("test-mock-player")) {
                if (player.level().getGameRules().get(booleanGameRule.get())) {
                    player.setHealth(player.getHealth() - player.level().getGameRules().get(integerGameRule.get()));
                }
            }
        });

        test.eventListeners().mod().addListener((RegisterGameRuleCategoryEvent event) -> event.register(category));

        reg.clientProvider(LanguageProvider.class, provider -> {
            // GameRuleCategory#getDescriptionId - this is not the translation key as one would expect, its the registry name
            // GameRuleCategory#label() - this uses #id to build the translation key by adding the below hardcoded prefix
            provider.add(category.id().toLanguageKey("gamerule.category"), "Custom GameRules");

            provider.add(booleanGameRule.value().getDescriptionId(), "Custom Boolean");
            provider.add(booleanGameRule.value().getDescriptionId() + ".description", "A custom boolean game rule");

            provider.add(integerGameRule.value().getDescriptionId(), "Custom Integer");
            provider.add(integerGameRule.value().getDescriptionId() + ".description", "A custom integer game rule");

            provider.add(doubleGameRule.value().getDescriptionId(), "Custom Double");
            provider.add(doubleGameRule.value().getDescriptionId() + ".description", "A custom double game rule");

            provider.add(stringGameRule.value().getDescriptionId(), "Custom String");
            provider.add(stringGameRule.value().getDescriptionId() + ".description", "A custom string game rule");
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);

            var gameRules = player.level().getGameRules();
            final var oldBool = gameRules.get(booleanGameRule.get());
            final var oldInt = gameRules.get(integerGameRule.get());
            final var oldDouble = gameRules.get(doubleGameRule.get());
            final var oldString = gameRules.get(stringGameRule.get());

            helper.startSequence()
                    .thenExecute(() -> gameRules.set(booleanGameRule.get(), true, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(integerGameRule.get(), 12, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(doubleGameRule.get(), 64D, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(stringGameRule.get(), "test", player.level().getServer()))

                    .thenIdle(1)
                    .thenExecute(() -> helper.assertEntityProperty(player, ServerPlayer::getHealth, "player health", 8f))

                    .thenExecute(() -> gameRules.set(booleanGameRule.get(), oldBool, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(integerGameRule.get(), oldInt, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(doubleGameRule.get(), oldDouble, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(stringGameRule.get(), oldString, player.level().getServer()))
                    .thenSucceed();
        });
    }
}
