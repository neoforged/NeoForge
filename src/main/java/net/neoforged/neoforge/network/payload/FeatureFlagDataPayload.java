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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record FeatureFlagDataPayload(Set<ResourceLocation> moddedFlags) implements CustomPacketPayload {
    public static final Type<FeatureFlagDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "feature_flags"));
    public static final StreamCodec<ByteBuf, FeatureFlagDataPayload> STREAM_CODEC = ResourceLocation.STREAM_CODEC
            .apply(ByteBufCodecs.<ByteBuf, ResourceLocation, Set<ResourceLocation>>collection(HashSet::new))
            .map(FeatureFlagDataPayload::new, FeatureFlagDataPayload::moddedFlags);

    @Override
    public Type<FeatureFlagDataPayload> type() {
        return TYPE;
    }
}
