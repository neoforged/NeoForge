/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.registration.PayloadRegistration;
import org.jetbrains.annotations.ApiStatus;

/**
 * Payload for the modded network query request.
 * Sent clientbound with no data to ask for the client's channels. The client will then reply with {@link #fromRegistry(Map)}.
 *
 * @param queries The query components
 */
@ApiStatus.Internal
public record ModdedNetworkQueryPayload(Map<ConnectionProtocol, Set<ModdedNetworkQueryComponent>> queries) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "register");
    public static final Type<ModdedNetworkQueryPayload> TYPE = new Type<>(ID);
    public static StreamCodec<FriendlyByteBuf, ModdedNetworkQueryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(IdentityHashMap::new,
                    ByteBufCodecs.idMapper(b -> ConnectionProtocol.values()[b], ConnectionProtocol::ordinal),
                    ByteBufCodecs.collection(HashSet::new, ModdedNetworkQueryComponent.STREAM_CODEC)),
            ModdedNetworkQueryPayload::queries, ModdedNetworkQueryPayload::new);

    @Override
    public Type<ModdedNetworkQueryPayload> type() {
        return TYPE;
    }

    public static ModdedNetworkQueryPayload fromRegistry(Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> registry) {
        var queries = registry.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().values().stream().map(ModdedNetworkQueryComponent::new).collect(Collectors.toSet())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new ModdedNetworkQueryPayload(queries);
    }
}
