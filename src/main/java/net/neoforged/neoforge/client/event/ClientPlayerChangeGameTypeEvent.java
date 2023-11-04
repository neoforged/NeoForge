/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when the client player is notified of a change of {@link GameType} from the server.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class ClientPlayerChangeGameTypeEvent extends Event {
    private final PlayerInfo info;
    private final GameType currentGameType;
    private final GameType newGameType;

    @ApiStatus.Internal
    public ClientPlayerChangeGameTypeEvent(PlayerInfo info, GameType currentGameType, GameType newGameType) {
        this.info = info;
        this.currentGameType = currentGameType;
        this.newGameType = newGameType;
    }

    /**
     * {@return the client player information}
     */
    public PlayerInfo getInfo() {
        return info;
    }

    /**
     * {@return the current game type of the player}
     */
    public GameType getCurrentGameType() {
        return currentGameType;
    }

    /**
     * {@return the new game type of the player}
     */
    public GameType getNewGameType() {
        return newGameType;
    }
}
