/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when the game type of a server player is changed to a different value than what it was previously. Eg Creative to Survival, not Survival to Survival.
 * If the event is cancelled the game mode of the player is not changed and the value of <code>newGameMode</code> is ignored.
 */
public class PlayerChangeGameModeEvent extends ServerPlayerEvent implements ICancellableEvent {
    private final GameType currentGameMode;
    private GameType newGameMode;

    public PlayerChangeGameModeEvent(ServerPlayer player, GameType currentGameMode, GameType newGameMode) {
        super(player);
        this.currentGameMode = currentGameMode;
        this.newGameMode = newGameMode;
    }

    public GameType getCurrentGameMode() {
        return currentGameMode;
    }

    public GameType getNewGameMode() {
        return newGameMode;
    }

    /**
     * Sets the game mode the player will be changed to if this event is not cancelled.
     */
    public void setNewGameMode(GameType newGameMode) {
        this.newGameMode = newGameMode;
    }
}
