/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

/**
 * ClientInformationUpdatedEvent is fired when a player changes server-synced client options,
 * specifically those in {@link net.minecraft.server.level.ClientInformation}.
 * <p>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 **/
public class ClientInformationUpdatedEvent extends PlayerEvent {
    private final ClientInformation oldInformation;
    private final ClientInformation updatedInformation;

    public ClientInformationUpdatedEvent(ServerPlayer player, ClientInformation oldInfo, ClientInformation newInfo) {
        super(player);
        this.oldInformation = oldInfo;
        this.updatedInformation = newInfo;
    }

    @Override
    public ServerPlayer getEntity() {
        return (ServerPlayer) super.getEntity();
    }

    /**
     * Returns the new client info to be applied to the player.
     * Sometimes the client resends unchanged options, so if that matters
     * for your use case, check equality with {@link #getOldInformation()}.
     *
     * @return updated information
     */
    public ClientInformation getUpdatedInformation() {
        return this.updatedInformation;
    }

    /**
     * Returns the existing client info from to the player.
     * <p>
     * May be blank or defaulted initial data on first event call for a player instance.
     *
     * @return updated information
     */
    public ClientInformation getOldInformation() {
        return this.oldInformation;
    }
}
