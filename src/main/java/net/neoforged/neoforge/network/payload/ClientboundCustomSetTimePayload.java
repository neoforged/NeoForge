/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ClientboundCustomSetTimePayload(long gameTime, long dayTime, boolean gameRule, float dayTimeFraction, float dayTimePerTick) implements CustomPacketPayload {
    public static final Type<ClientboundCustomSetTimePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "custom_time_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCustomSetTimePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, ClientboundCustomSetTimePayload::gameTime,
            ByteBufCodecs.VAR_LONG, ClientboundCustomSetTimePayload::dayTime,
            ByteBufCodecs.BOOL, ClientboundCustomSetTimePayload::gameRule,
            ByteBufCodecs.FLOAT, ClientboundCustomSetTimePayload::dayTimeFraction,
            ByteBufCodecs.FLOAT, ClientboundCustomSetTimePayload::dayTimePerTick,
            ClientboundCustomSetTimePayload::new);

    @Override
    public Type<ClientboundCustomSetTimePayload> type() {
        return TYPE;
    }
}
