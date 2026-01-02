/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record FeatureFlagDataPayload(Set<Identifier> moddedFlags) implements CustomPacketPayload {
    public static final Type<FeatureFlagDataPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "feature_flags"));
    public static final StreamCodec<ByteBuf, FeatureFlagDataPayload> STREAM_CODEC = Identifier.STREAM_CODEC
            .apply(ByteBufCodecs.<ByteBuf, Identifier, Set<Identifier>>collection(HashSet::new))
            .map(FeatureFlagDataPayload::new, FeatureFlagDataPayload::moddedFlags);

    @Override
    public Type<FeatureFlagDataPayload> type() {
        return TYPE;
    }
}
