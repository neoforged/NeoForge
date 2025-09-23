/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.api.SchemaComponent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public final class NeoForgeSchemas {
    private NeoForgeSchemas() {}

    public static final SchemaComponent MOD_SCHEMA = Schema.registerSchema(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "mod"), Schema.record()
            .withField("modId", Schema.STRING_SCHEMA)
            .withField("version", Schema.STRING_SCHEMA)
            .withField("displayName", Schema.STRING_SCHEMA)
            .withField("description", Schema.STRING_SCHEMA));
}
