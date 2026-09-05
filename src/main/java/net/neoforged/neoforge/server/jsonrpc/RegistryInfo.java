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

public record RegistryInfo(Identifier registryName, long size, List<Identifier> entries) {
    private static final List<Identifier> EMPTY_ENTRIES = List.of();
    public static final Codec<RegistryInfo> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("registryName").forGetter(RegistryInfo::registryName),
            Codec.LONG.fieldOf("size").forGetter(RegistryInfo::size),
            Identifier.CODEC.listOf().fieldOf("entries").forGetter(RegistryInfo::entries)).apply(inst, RegistryInfo::new));
    public static final Codec<RegistryInfo> CODEC_WITHOUT_ENTRIES = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("registryName").forGetter(RegistryInfo::registryName),
            Codec.LONG.fieldOf("size").forGetter(RegistryInfo::size)).apply(inst, RegistryInfo::new));

    private RegistryInfo(Identifier registryName, long size) {
        this(registryName, size, EMPTY_ENTRIES);
    }

    static RegistryInfo withoutEntries(HolderLookup.RegistryLookup<?> lookup) {
        return new RegistryInfo(lookup.key().identifier(), lookup.listElementIds().count());
    }

    static RegistryInfo withEntries(HolderLookup.RegistryLookup<?> lookup) {
        List<Identifier> list = lookup.listElementIds().map(ResourceKey::identifier).toList();
        return new RegistryInfo(lookup.key().identifier(), list.size(), list);
    }
}
