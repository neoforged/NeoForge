/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.server;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.jsonrpc.JsonRpc;
import net.minecraft.server.jsonrpc.ManagementServer;
import net.minecraft.server.jsonrpc.api.SchemaComponent;
import net.minecraft.server.notifications.NotificationManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/// Fired to register [`SchemaComponents`][SchemaComponent] used in incoming and outgoing JSON-RPC methods.
///
/// This event is [non-cancellable][ICancellableEvent].
///
/// This event is fired on the [game event bus][NeoForge#EVENT_BUS], 
/// only on the [dedicated server][Dist#DEDICATED_SERVER] 
/// before the [ManagementServer] is instantiated in [JsonRpc#create(DedicatedServerSettings, NotificationManager)].
public class RegisterRpcSchemaEvent extends Event {
    private final Map<String, SchemaComponent<?>> components;

    @ApiStatus.Internal
    public RegisterRpcSchemaEvent(Map<String, SchemaComponent<?>> components) {
        this.components = components;
    }

    /// Registers a [SchemaComponent].
    ///
    /// @param component The component to register.
    /// @throws IllegalArgumentException if a component with the same name is already registered.
    public void register(SchemaComponent<?> component) {
        Identifier key = Identifier.parse(component.name());
        if (components.containsKey(key.toString())) {
            throw new IllegalArgumentException("Duplicate SchemaComponent: " + key);
        }
        components.put(key.toString(), component);
    }
}
