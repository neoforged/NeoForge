/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

record ModRecord(String modId, String version, String displayName, String description) {
    public static final Codec<ModRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("modId").forGetter(ModRecord::modId),
            Codec.STRING.fieldOf("version").forGetter(ModRecord::version),
            Codec.STRING.fieldOf("displayName").forGetter(ModRecord::displayName),
            Codec.STRING.fieldOf("description").forGetter(ModRecord::description)).apply(instance, ModRecord::new));
}
