/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.IRegistryExtension;

public record RegistryInfo(Identifier registryName, Optional<Boolean> synced, List<Identifier> entries) {
    public static final Codec<RegistryInfo> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("registryName").forGetter(RegistryInfo::registryName),
            Codec.BOOL.optionalFieldOf("synced").forGetter(RegistryInfo::synced),
            Identifier.CODEC.listOf().fieldOf("entries").forGetter(RegistryInfo::entries)).apply(inst, RegistryInfo::new));

    static RegistryInfo withEntries(HolderLookup.RegistryLookup<?> lookup) {
        List<Identifier> list = lookup.listElementIds().map(ResourceKey::identifier).toList();
        return new RegistryInfo(lookup.key().identifier(), lookup instanceof IRegistryExtension<?> ext ? Optional.of(ext.doesSync()) : Optional.empty(), list);
    }
}
