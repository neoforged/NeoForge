/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.server;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.ManagementServer;
import net.minecraft.server.jsonrpc.api.SchemaComponent;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Event to register {@link SchemaComponent SchemaComponents} used in incoming and outgoing JSON-RPC methods.<br>
 * Fired before the {@link ManagementServer} is instantiated in {@link DedicatedServer#initServer()}.
 */
public class RegisterRpcSchemaEvent extends Event {
    private final Map<String, SchemaComponent> components;

    @Internal
    public RegisterRpcSchemaEvent(Map<String, SchemaComponent> components) {
        this.components = components;
    }

    /**
     * Registers a {@link SchemaComponent}.
     * 
     * @param component The component to register.
     */
    public void register(SchemaComponent component) {
        ResourceLocation key = ResourceLocation.parse(component.name());
        if (components.containsKey(key.toString()) || components.containsValue(component)) {
            throw new IllegalArgumentException("Duplicate SchemaComponent: " + key);
        }
        components.put(key.toString(), component);
    }
}
