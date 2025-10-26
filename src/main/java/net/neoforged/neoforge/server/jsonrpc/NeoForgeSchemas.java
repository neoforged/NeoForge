/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.api.SchemaComponent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.server.RegisterSchemaEvent;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class NeoForgeSchemas {
    private NeoForgeSchemas() {}

    static final SchemaComponent REGISTRY_SCHEMA = new SchemaComponent(ResourceLocation.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "registry"), Schema.record()
            .withField("registryName", Schema.STRING_SCHEMA));
    static final SchemaComponent REGISTRY_SCHEMA_WITH_ENTRIES = new SchemaComponent(ResourceLocation.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "registry_with_entries"), Schema.record()
            .withField("registryName", Schema.STRING_SCHEMA)
            .withField("entries", Schema.STRING_SCHEMA.asArray()));
    static final SchemaComponent MOD_SCHEMA = new SchemaComponent(ResourceLocation.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "mod"), Schema.record()
            .withField("modId", Schema.STRING_SCHEMA)
            .withField("version", Schema.STRING_SCHEMA)
            .withField("displayName", Schema.STRING_SCHEMA)
            .withField("description", Schema.STRING_SCHEMA));

    public static void registerSchemas(RegisterSchemaEvent event) {
        event.register(MOD_SCHEMA);
        event.register(REGISTRY_SCHEMA);
        event.register(REGISTRY_SCHEMA_WITH_ENTRIES);
    }
}
