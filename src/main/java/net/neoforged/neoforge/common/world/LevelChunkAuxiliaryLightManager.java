/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LightEngine;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.payload.AuxiliaryLightDataPayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class LevelChunkAuxiliaryLightManager implements AuxiliaryLightManager, INBTSerializable<ListTag> {
    public static final String LIGHT_NBT_KEY = "neoforge:aux_lights";

    private final LevelChunk owner;
    private final Map<BlockPos, Byte> lights = new ConcurrentHashMap<>();

    public LevelChunkAuxiliaryLightManager(LevelChunk owner) {
        this.owner = owner;
    }

    @Override
    public void setLightAt(BlockPos pos, int value) {
        pos = pos.immutable();
        value = Mth.clamp(value, 0, LightEngine.MAX_LEVEL);

        Byte oldValue;
        if (value > 0) {
            oldValue = lights.put(pos, (byte) value);
        } else {
            oldValue = lights.remove(pos);
        }
        if (Objects.requireNonNullElse(oldValue, (byte) 0) != value) {
            owner.getLevel().getChunkSource().getLightEngine().checkBlock(pos);
            owner.setUnsaved(true);
        }
    }

    @Override
    public int getLightAt(BlockPos pos) {
        return lights.getOrDefault(pos, (byte) 0);
    }

    @Override
    public boolean canSerialize() {
        return !lights.isEmpty();
    }

    @Override
    public ListTag serializeNBT() {
        ListTag list = new ListTag();
        lights.forEach((pos, light) -> {
            CompoundTag tag = new CompoundTag();
            tag.putLong("pos", pos.asLong());
            tag.putByte("level", light);
            list.add(tag);
        });
        return list;
    }

    @Override
    public void deserializeNBT(ListTag list) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            lights.put(BlockPos.of(tag.getLong("pos")), tag.getByte("level"));
        }
    }

    public Packet<?> sendLightDataTo(ClientboundLevelChunkWithLightPacket chunkPacket) {
        return new ClientboundBundlePacket(List.of(chunkPacket, new ClientboundCustomPayloadPacket(
                new AuxiliaryLightDataPayload(owner.getPos(), Map.copyOf(lights)))));
    }

    public void handleLightDataSync(Map<BlockPos, Byte> lights) {
        this.lights.clear();
        this.lights.putAll(lights);
    }
}
