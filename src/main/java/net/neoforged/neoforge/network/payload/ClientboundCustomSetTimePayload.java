/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.clock.WorldClock;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ClientboundCustomSetTimePayload(
        long gameTime,
        Map<Holder<WorldClock>, ExtendedClockState> clockUpdates) implements CustomPacketPayload {
    public static final Type<ClientboundCustomSetTimePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "custom_time_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCustomSetTimePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, ClientboundCustomSetTimePayload::gameTime,
            ByteBufCodecs.map(HashMap::new, WorldClock.STREAM_CODEC, ExtendedClockState.STREAM_CODEC), ClientboundCustomSetTimePayload::clockUpdates,
            ClientboundCustomSetTimePayload::new);

    @Override
    public Type<ClientboundCustomSetTimePayload> type() {
        return TYPE;
    }
}
