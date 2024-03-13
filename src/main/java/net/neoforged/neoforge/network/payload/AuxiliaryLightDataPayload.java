/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record AuxiliaryLightDataPayload(ChunkPos pos, Map<BlockPos, Byte> entries) implements CustomPacketPayload {
    public static final Type<AuxiliaryLightDataPayload> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "auxiliary_light_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AuxiliaryLightDataPayload> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.CHUNK_POS,
            AuxiliaryLightDataPayload::pos,
            ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, ByteBufCodecs.BYTE),
            AuxiliaryLightDataPayload::entries,
            AuxiliaryLightDataPayload::new);

    @Override
    public Type<AuxiliaryLightDataPayload> type() {
        return TYPE;
    }
}
