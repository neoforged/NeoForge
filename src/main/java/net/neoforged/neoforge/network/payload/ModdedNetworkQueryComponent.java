/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;

public record ModdedNetworkQueryComponent(ResourceLocation id, Optional<String> version, Optional<PacketFlow> flow, boolean optional) {

    public ModdedNetworkQueryComponent(FriendlyByteBuf buf) {
        this(
                buf.readResourceLocation(),
                buf.readOptional(FriendlyByteBuf::readUtf),
                buf.readOptional(buffer -> buffer.readEnum(PacketFlow.class)),
                buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeOptional(version, FriendlyByteBuf::writeUtf);
        buf.writeOptional(flow, FriendlyByteBuf::writeEnum);
        buf.writeBoolean(optional);
    }
}
