/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.extensions.IFriendlyByteBufExtension;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public record AuxiliaryLightDataPayload(ChunkPos pos, Map<BlockPos, Byte> entries) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "auxiliary_light_data");

    public AuxiliaryLightDataPayload(FriendlyByteBuf buf) {
        this(buf.readChunkPos(), buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readByte));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
        buf.writeMap(entries, FriendlyByteBuf::writeBlockPos, IFriendlyByteBufExtension::writeByte);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
