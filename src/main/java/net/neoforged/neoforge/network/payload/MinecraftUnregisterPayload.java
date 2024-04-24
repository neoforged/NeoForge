/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record MinecraftUnregisterPayload(Set<ResourceLocation> forgottenChannels) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("unregister");
    public static final Type<MinecraftUnregisterPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, MinecraftUnregisterPayload> STREAM_CODEC = DinnerboneProtocolUtils.CHANNELS_CODEC.map(MinecraftUnregisterPayload::new, MinecraftUnregisterPayload::forgottenChannels);

    @Override
    public Type<MinecraftUnregisterPayload> type() {
        return TYPE;
    }
}
