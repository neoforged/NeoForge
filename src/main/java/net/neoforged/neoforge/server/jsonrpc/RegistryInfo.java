/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record RegistryInfo(Identifier registryName, List<Identifier> entries) {
    private static final List<Identifier> EMPTY_ENTRIES = List.of();
    public static final Codec<RegistryInfo> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("registryName").forGetter(RegistryInfo::registryName),
            Identifier.CODEC.listOf().optionalFieldOf("entries", EMPTY_ENTRIES).forGetter(RegistryInfo::entries)).apply(inst, RegistryInfo::new));

    static RegistryInfo from(HolderLookup.RegistryLookup<?> lookup, boolean includeEntries) {
        return new RegistryInfo(lookup.key().identifier(), includeEntries ? lookup.listElementIds().map(ResourceKey::identifier).toList() : EMPTY_ENTRIES);
    }
}
