/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.neoforged.bus.api.Event;

/**
 * Fired when tags are updated on either server or client. This event can be used to refresh data that depends on tags.
 */
public sealed class TagsUpdatedEvent extends Event {
    private final RegistryAccess registries;
    private final UpdateCause updateCause;
    private final boolean integratedServer;

    protected TagsUpdatedEvent(RegistryAccess registries, boolean fromClientPacket, boolean isIntegratedServerConnection) {
        this.registries = registries;
        this.updateCause = fromClientPacket ? UpdateCause.CLIENT_PACKET_RECEIVED : UpdateCause.SERVER_DATA_LOAD;
        this.integratedServer = isIntegratedServerConnection;
    }

    /// {@return the registries that have had their tags rebound}
    public RegistryAccess getRegistries() {
        return registries;
    }

    /// @return The dynamic registries that have had their tags rebound.
    ///
    /// @deprecated Use [#getRegistries()] instead
    @Deprecated
    public HolderLookup.Provider getLookupProvider() {
        return registries;
    }

    /**
     * @return the cause for this tag update
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

    /// Fired when tags are updated following a server datapack (re)load
    public static final class ServerDataLoad extends TagsUpdatedEvent {
        private final ReloadableServerResources serverResources;

        public ServerDataLoad(ReloadableServerResources serverResources, RegistryAccess registries) {
            super(registries, false, false);
            this.serverResources = serverResources;
        }

        /// {@return the server resources which triggered this tag update}
        public ReloadableServerResources getServerResources() {
            return serverResources;
        }
    }

    /// Fired when tags are updated by the client receiving tag data from the server
    public static final class ClientPacketReceived extends TagsUpdatedEvent {
        public ClientPacketReceived(RegistryAccess registries, boolean isIntegratedServerConnection) {
            super(registries, true, isIntegratedServerConnection);
        }
    }

    /**
     * Represents the cause for a tag update.
     */
    public enum UpdateCause {
        /**
         * The tag update is caused by the server loading datapack data. Note that in single player this still happens
         * on the client thread.
         */
        SERVER_DATA_LOAD,
        /**
         * The tag update is caused by the client receiving the tag data from the server.
         */
        CLIENT_PACKET_RECEIVED
    }
}
