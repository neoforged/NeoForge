/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.core.Holder;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is emitted after data components have been bound to holders in a registry.
 * <p>This can occur on the server when datapacks are reloaded or when the client receives this data upon joining a world.
 * <p>This event signals when {@link Holder#areComponentsBound()} becomes true.
 */
public class DefaultDataComponentsBoundEvent extends Event {
    private final UpdateCause updateCause;
    private final boolean integratedServer;

    @ApiStatus.Internal
    public DefaultDataComponentsBoundEvent(boolean fromClientPacket, boolean isIntegratedServerConnection) {
        this.updateCause = fromClientPacket ? UpdateCause.CLIENT_PACKET_RECEIVED : UpdateCause.SERVER_DATA_LOAD;
        this.integratedServer = isIntegratedServerConnection;
    }

    /**
     * @return the cause for binding the default data components
     */
    public UpdateCause getUpdateCause() {
        return updateCause;
    }

    /**
     * Whether static data (which in single player is shared between server and client thread) should be updated as a
     * result of this event. Effectively this means that in single player only the server-side updates this data.
     */
    public boolean shouldUpdateStaticData() {
        return updateCause == UpdateCause.SERVER_DATA_LOAD || !integratedServer;
    }

    /**
     * Represents the cause for binding the default data components.
     */
    public enum UpdateCause {
        /**
         * The default data components were bound due to the server loading datapack data. Note that in single player this still happens
         * on the client thread.
         */
        SERVER_DATA_LOAD,
        /**
         * The default data components were bound due to the client receiving the datapack data from the server.
         */
        CLIENT_PACKET_RECEIVED
    }
}
