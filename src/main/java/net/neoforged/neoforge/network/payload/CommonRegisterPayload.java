/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Common Register, used to send play-phase channels during the configuration phase.
 * 
 * @param version  Declared version of all contained channels. Currently unused by NeoForge.
 * @param protocol The {@link ConnectionProtocol} of the contained channels. One of "play" or "configuration". Currently NeoForge only expects and sends "play" channels via this payload.
 * @param channels A list of all named channels available for the declared phase.
 */
@ApiStatus.Internal
public record CommonRegisterPayload(int version, ConnectionProtocol protocol, Set<ResourceLocation> channels) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("c", "register");
    public static final CustomPacketPayload.Type<CommonRegisterPayload> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, CommonRegisterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CommonRegisterPayload::version,
            ByteBufCodecs.STRING_UTF8.map(CommonRegisterPayload::protocolById, ConnectionProtocol::id), CommonRegisterPayload::protocol,
            ByteBufCodecs.collection(HashSet::new, ResourceLocation.STREAM_CODEC), CommonRegisterPayload::channels,
            CommonRegisterPayload::new);

    @Override
    public Type<CommonRegisterPayload> type() {
        return TYPE;
    }

    @Nullable
    private static ConnectionProtocol protocolById(String id) {
        for (ConnectionProtocol p : ConnectionProtocol.values()) {
            if (p.id().equals(id)) {
                return p;
            }
        }
        return null;
    }
}
