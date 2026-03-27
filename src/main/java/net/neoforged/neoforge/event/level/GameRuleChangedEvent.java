/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event which is fired when ever a {@link GameRule} is updated.
 * <p>
 * This event is fired on the {@link NeoForge#EVENT_BUS main NeoForge event bus}, only on the {@linkplain LogicalSide#SERVER logical server}.
 */
public final class GameRuleChangedEvent extends Event {
    private final MinecraftServer server;
    private final GameRule<?> gameRule;
    private final Object newValue;

    @ApiStatus.Internal
    public <T> GameRuleChangedEvent(MinecraftServer server, GameRule<T> gameRule, T newValue) {
        this.server = server;
        this.gameRule = gameRule;
        this.newValue = newValue;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public GameRules getGameRules() {
        return server.getGameRules();
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
