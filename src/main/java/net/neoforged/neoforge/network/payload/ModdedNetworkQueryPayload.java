/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.registration.PayloadRegistration;
import org.jetbrains.annotations.ApiStatus;

/**
 * Payload for the modded network query request
 *
 * @param configuration The configuration components
 * @param play          The play components
 */
@ApiStatus.Internal
public record ModdedNetworkQueryPayload(Map<ConnectionProtocol, Set<ModdedNetworkQueryComponent>> queries) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "register");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = ModdedNetworkQueryPayload::new;

    public ModdedNetworkQueryPayload() {
        this(Collections.emptyMap());
    }

    public ModdedNetworkQueryPayload(FriendlyByteBuf byteBuf) {
        this(byteBuf.readMap(b -> b.readEnum(ConnectionProtocol.class), buf -> buf.readCollection(HashSet::new, ModdedNetworkQueryComponent::new)));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(this.queries, FriendlyByteBuf::writeEnum, (b, set) -> b.writeObjectCollection(set, ModdedNetworkQueryComponent::write));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static ModdedNetworkQueryPayload fromRegistry(Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> registry) {
        var queries = registry.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().values().stream().map(ModdedNetworkQueryComponent::new).collect(Collectors.toSet())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new ModdedNetworkQueryPayload(queries);
    }
}
