/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record RegistryInfo(ResourceLocation registryName, List<ResourceLocation> entries) {
    private static final List<ResourceLocation> EMPTY_ENTRIES = List.of();
    public static final Codec<RegistryInfo> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("registryName").forGetter(RegistryInfo::registryName),
            ResourceLocation.CODEC.listOf().optionalFieldOf("entries", EMPTY_ENTRIES).forGetter(RegistryInfo::entries)).apply(inst, RegistryInfo::new));

    static RegistryInfo from(HolderLookup.RegistryLookup<?> lookup, boolean includeEntries) {
        return new RegistryInfo(lookup.key().location(), includeEntries ? lookup.listElementIds().map(ResourceKey::location).toList() : EMPTY_ENTRIES);
    }
}
