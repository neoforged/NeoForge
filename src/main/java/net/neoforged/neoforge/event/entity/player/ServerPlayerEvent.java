/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

// TODO implement PlayerEvent?
public abstract class ServerPlayerEvent extends LivingEvent {
    public ServerPlayerEvent(ServerPlayer entity) {
        super(entity);
    }

    public ServerPlayer getEntity() {
        return ((ServerPlayer) super.getEntity());
    }

    public static class PlayerLoggedInEvent extends ServerPlayerEvent {
        public PlayerLoggedInEvent(ServerPlayer player) {
            super(player);
        }
    }

    public static class PlayerLoggedOutEvent extends ServerPlayerEvent {
        public PlayerLoggedOutEvent(ServerPlayer player) {
            super(player);
        }
    }

    public static class PlayerRespawnEvent extends ServerPlayerEvent {
        private final boolean endConquered;

        public PlayerRespawnEvent(ServerPlayer player, boolean endConquered) {
            super(player);
            this.endConquered = endConquered;
        }

        /**
         * Did this respawn event come from the player conquering the end?
         *
         * @return if this respawn was because the player conquered the end
         */
        public boolean isEndConquered() {
            return this.endConquered;
        }
    }

    public static class PlayerChangedDimensionEvent extends ServerPlayerEvent {
        private final ResourceKey<Level> originalDimension;
        private final ResourceKey<Level> newDimension;

        public PlayerChangedDimensionEvent(ServerPlayer player, ResourceKey<Level> originalDimension, ResourceKey<Level> newDimension) {
            super(player);
            this.originalDimension = originalDimension;
            this.newDimension = newDimension;
        }

        public ResourceKey<Level> getOriginalDimension() {
            return this.originalDimension;
        }

        public ResourceKey<Level> getNewDimension() {
            return this.newDimension;
        }
    }

    /**
     * Fired when the game type of a server player is changed to a different value than what it was previously. Eg Creative to Survival, not Survival to Survival.
     * If the event is cancelled the game mode of the player is not changed and the value of <code>newGameMode</code> is ignored.
     */
    public static class PlayerChangeGameModeEvent extends ServerPlayerEvent implements ICancellableEvent {
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
}
