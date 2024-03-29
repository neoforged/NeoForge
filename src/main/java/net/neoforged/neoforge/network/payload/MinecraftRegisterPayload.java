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

public record MinecraftRegisterPayload(Set<ResourceLocation> newChannels) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("register");
    public static final Type<MinecraftRegisterPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, MinecraftRegisterPayload> STREAM_CODEC = DinnerboneProtocolUtils.CHANNELS_CODEC.map(MinecraftRegisterPayload::new, MinecraftRegisterPayload::newChannels);

    @Override
    public Type<MinecraftRegisterPayload> type() {
        return TYPE;
    }
}
