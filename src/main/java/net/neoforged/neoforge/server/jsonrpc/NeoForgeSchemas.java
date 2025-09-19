/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import net.minecraft.server.jsonrpc.api.ReferenceUtil;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.api.SchemaComponent;

public final class NeoForgeSchemas {
    private NeoForgeSchemas() {}

    public static final SchemaComponent MOD_SCHEMA = registerSchema("mod", Schema.record()
            .withField("modId", Schema.STRING_SCHEMA.flatten())
            .withField("version", Schema.STRING_SCHEMA.flatten())
            .withField("displayName", Schema.STRING_SCHEMA.flatten())
            .withField("description", Schema.STRING_SCHEMA.flatten()));

    private static SchemaComponent registerSchema(String name, Schema schema) {
        SchemaComponent schemacomponent = new SchemaComponent("neoforge:" + name, ReferenceUtil.createLocalReference(name), schema);
        Schema.getSchemaRegistry().add(schemacomponent);
        return schemacomponent;
    }
}
