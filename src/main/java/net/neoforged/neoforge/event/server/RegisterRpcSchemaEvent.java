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
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/// Event to register [`SchemaComponents`][SchemaComponent] used in incoming and outgoing JSON-RPC methods.
///
/// Fired before the [ManagementServer] is instantiated in [JsonRpc#create(DedicatedServerSettings, NotificationManager)].
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
