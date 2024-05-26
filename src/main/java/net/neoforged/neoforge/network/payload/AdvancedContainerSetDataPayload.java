/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * A custom payload that updates the full dataslot value instead of just the short value
 *
 * @param containerId The containerId for the container.
 * @param dataId      The ID of the dataslot.
 * @param value       The value of the dataslot.
 */
@ApiStatus.Internal
public record AdvancedContainerSetDataPayload(byte containerId, short dataId, int value) implements CustomPacketPayload {
    public static final Type<AdvancedContainerSetDataPayload> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "advanced_container_set_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedContainerSetDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            AdvancedContainerSetDataPayload::containerId,
            ByteBufCodecs.SHORT,
            AdvancedContainerSetDataPayload::dataId,
            ByteBufCodecs.VAR_INT,
            AdvancedContainerSetDataPayload::value,
            AdvancedContainerSetDataPayload::new);

    @Override
    public Type<AdvancedContainerSetDataPayload> type() {
        return TYPE;
    }

    public ClientboundContainerSetDataPacket toVanillaPacket() {
        return new ClientboundContainerSetDataPacket(containerId, dataId, value);
    }
}
