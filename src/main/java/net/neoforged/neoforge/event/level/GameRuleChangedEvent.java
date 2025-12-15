/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Event which is fired when ever a {@link GameRule} is updated.
 * <p>
 * This event is fired on the {@link NeoForge#EVENT_BUS main NeoForge event bus}, only on both the logical {@linkplain LogicalSide#CLIENT client} and {@linkplain LogicalSide#SERVER}.
 */
public final class GameRuleChangedEvent extends Event {
    @Nullable
    private final MinecraftServer server;
    private final GameRules gameRules;
    private final GameRule<?> gameRule;
    private final Object newValue;

    @ApiStatus.Internal
    public <T> GameRuleChangedEvent(@Nullable MinecraftServer server, GameRules gameRules, GameRule<T> gameRule, T newValue) {
        this.server = server;
        this.gameRules = gameRules;
        this.gameRule = gameRule;
        this.newValue = newValue;
    }

    /**
     * {@return The active server instance holding the game rules.}
     *
     * @apiNote Note that not all game rule state changes pass along a server instance, For example the {@linkplain net.neoforged.neoforge.server.command.TimeSpeedCommand#setSpeed(CommandSourceStack, float) TimeSpeedCommand}
     *          updates {@link GameRules#ADVANCE_TIME} while passing a {@code null} server instance.
     */
    @Nullable
    public MinecraftServer getServer() {
        return server;
    }

    public GameRules getGameRules() {
        return gameRules;
    }

    public GameRule<?> getGameRule() {
        return gameRule;
    }

    public Object getNewValue() {
        return newValue;
    }

    /**
     * Executes the given {@code action} if the updated {@link GameRule} matches {@code gameRule}.
     *
     * @param gameRule {@link GameRule} to validate aganst.
     * @param action   Action to be invoked if the updated game rule matches, passing in the updated value.
     * @param <T>      Game rule data type.
     */
    @SuppressWarnings("unchecked")
    public <T> void runIfMatching(GameRule<T> gameRule, Consumer<T> action) {
        if (this.gameRule == gameRule) {
            action.accept((T) newValue);
        }
    }
}
