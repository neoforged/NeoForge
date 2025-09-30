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

/**
 * Event to register {@link SchemaComponent SchemaComponents} used in incoming and outgoing JSON-RPC methods.<br>
 * Fired before the {@link ManagementServer} is instantiated in {@link DedicatedServer#initServer()}.
 */
public class RegisterSchemaEvent extends Event {
    private final Map<ResourceLocation, SchemaComponent> components;

    public RegisterSchemaEvent(Map<ResourceLocation, SchemaComponent> components) {
        this.components = components;
    }

    public void register(SchemaComponent component) {
        components.put(ResourceLocation.tryParse(component.name()), component);
    }
}
