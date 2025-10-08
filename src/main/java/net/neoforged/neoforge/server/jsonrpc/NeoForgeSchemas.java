/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.api.SchemaComponent;
import net.neoforged.neoforge.event.server.RegisterSchemaEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class NeoForgeSchemas {
    private NeoForgeSchemas() {}

    public static final SchemaComponent MOD_SCHEMA = new SchemaComponent(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "mod"), Schema.record()
            .withField("modId", Schema.STRING_SCHEMA)
            .withField("version", Schema.STRING_SCHEMA)
            .withField("displayName", Schema.STRING_SCHEMA)
            .withField("description", Schema.STRING_SCHEMA));

    public static void registerSchemas(RegisterSchemaEvent event) {
        event.register(MOD_SCHEMA);
    }
}
