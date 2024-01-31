/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * A custom payload that updates the full dataslot value instead of just the short value
 *
 * @param containerId  The containerId for the container.
 * @param dataId       The id for the dataslot.
 * @param value        The value for the dataslot.
 */
@ApiStatus.Internal
public record AdvancedContainerSetDataPayload(int containerId, int dataId, int value) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "advanced_set_data");

    public AdvancedContainerSetDataPayload(FriendlyByteBuf buffer) {
        this(buffer.readByte(), buffer.readShort(), buffer.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(containerId);
        buffer.writeShort(dataId);
        buffer.writeVarInt(value);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public ClientboundContainerSetDataPacket toVanillaPacket() {
        return new ClientboundContainerSetDataPacket(containerId, dataId, value);
    }
}
