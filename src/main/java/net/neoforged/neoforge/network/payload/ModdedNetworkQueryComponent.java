/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.PayloadRegistration;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a potential modded network component, used for querying the client for modded network components.
 *
 * @param id       The id of the component
 * @param version  The version of the component, if present
 * @param flow     The flow of the component, if present
 * @param optional Whether the component is optional
 */
@ApiStatus.Internal
public record ModdedNetworkQueryComponent(ResourceLocation id, Optional<String> version, Optional<PacketFlow> flow, boolean optional) {
    public ModdedNetworkQueryComponent(PayloadRegistration<?> reg) {
        this(reg.id(), reg.version(), reg.flow(), reg.optional());
    }

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
