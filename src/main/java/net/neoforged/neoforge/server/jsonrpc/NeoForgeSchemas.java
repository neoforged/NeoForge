/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.api.SchemaComponent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.server.RegisterRpcSchemaEvent;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class NeoForgeSchemas {
    private NeoForgeSchemas() {}

    static final SchemaComponent<RegistryInfo> REGISTRY_SCHEMA_WITHOUT_ENTRIES = new SchemaComponent<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "registry_without_entries"), Schema.record(RegistryInfo.CODEC_WITHOUT_ENTRIES)
            .withField("registryName", Schema.STRING_SCHEMA)
            .withField("size", Schema.INT_SCHEMA));
    static final SchemaComponent<RegistryInfo> REGISTRY_SCHEMA = new SchemaComponent<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "registry"), Schema.record(RegistryInfo.CODEC)
            .withField("registryName", Schema.STRING_SCHEMA)
            .withField("size", Schema.INT_SCHEMA)
            .withField("entries", Schema.STRING_SCHEMA.asArray()));
    static final SchemaComponent<ModRecord> MOD_SCHEMA = new SchemaComponent<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "mod"), Schema.record(ModRecord.CODEC)
            .withField("modId", Schema.STRING_SCHEMA)
            .withField("version", Schema.STRING_SCHEMA)
            .withField("displayName", Schema.STRING_SCHEMA)
            .withField("description", Schema.STRING_SCHEMA));

    public static void registerSchemas(RegisterRpcSchemaEvent event) {
        event.register(MOD_SCHEMA);
        event.register(REGISTRY_SCHEMA_WITHOUT_ENTRIES);
        event.register(REGISTRY_SCHEMA);
    }
}
