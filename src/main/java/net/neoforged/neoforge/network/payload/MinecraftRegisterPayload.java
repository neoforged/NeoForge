/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MinecraftRegisterPayload(Set<ResourceLocation> newChannels) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("register");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = MinecraftRegisterPayload::new;

    public MinecraftRegisterPayload(FriendlyByteBuf buf) {
        this(DinnerboneProtocolUtils.readChannels(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        DinnerboneProtocolUtils.writeChannels(buf, newChannels);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
