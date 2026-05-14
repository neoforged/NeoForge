/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/// Fired when tags are updated on either server or client. This event can be used to refresh data that depends on tags.
///
/// Listeners intending to use this event to update static data guarded by [#shouldUpdateStaticData()] should listen to
/// the root event. Listeners interested in the side-specific notification or the side-specific context should listen to
/// the [TagsUpdatedEvent.ServerDataLoad] and [TagsUpdatedEvent.ClientPacketReceived] subclasses instead.
public sealed class TagsUpdatedEvent extends Event {
    private final RegistryAccess registries;

    protected TagsUpdatedEvent(RegistryAccess registries) {
        this.registries = registries;
    }

    /// {@return the registries that have had their tags rebound}
    public RegistryAccess getRegistries() {
        return registries;
    }

    /// @return The dynamic registries that have had their tags rebound.
    ///
    /// @deprecated Use [#getRegistries()] instead
    @Deprecated(forRemoval = true, since = "26.1.2")
    public HolderLookup.Provider getLookupProvider() {
        return registries;
    }

    /// {@return the cause for this tag update}
    ///
    /// @deprecated Subscribe to subclasses instead
    @Deprecated(forRemoval = true, since = "26.1.2")
    public UpdateCause getUpdateCause() {
        return UpdateCause.SERVER_DATA_LOAD;
    }

    /// Whether static data (which in single player is shared between server and client thread) should be updated as a
    /// result of this event. Effectively this means that in single player only the server-side updates this data.
    public boolean shouldUpdateStaticData() {
        return true;
    }

    /// Fired when tags are updated following a server datapack (re)load
    public static final class ServerDataLoad extends TagsUpdatedEvent {
        private final ReloadableServerResources serverResources;

        @ApiStatus.Internal
        public ServerDataLoad(ReloadableServerResources serverResources, RegistryAccess registries) {
            super(registries);
            this.serverResources = serverResources;
        }

        /// {@return the server resources which triggered this tag update}
        public ReloadableServerResources getServerResources() {
            return serverResources;
        }
    }

    /// Fired when tags are updated by the client receiving tag data from the server
    public static final class ClientPacketReceived extends TagsUpdatedEvent {
        private final boolean integratedServer;

        @ApiStatus.Internal
        public ClientPacketReceived(RegistryAccess registries, boolean isIntegratedServerConnection) {
            super(registries);
            this.integratedServer = isIntegratedServerConnection;
        }

        @Override
        @Deprecated(forRemoval = true, since = "26.1.2")
        public UpdateCause getUpdateCause() {
            return UpdateCause.CLIENT_PACKET_RECEIVED;
        }

        @Override
        public boolean shouldUpdateStaticData() {
            return !integratedServer;
        }
    }

    /// Represents the cause for a tag update.
    @Deprecated(forRemoval = true, since = "26.1.2")
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
