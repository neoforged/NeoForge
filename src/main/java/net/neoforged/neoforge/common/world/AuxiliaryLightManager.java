/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import com.google.common.base.Preconditions;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LightEngine;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.payload.AuxiliaryLightDataPayload;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manager for light values controlled by dynamic data in {@link BlockEntity}s.
 */
public final class AuxiliaryLightManager implements INBTSerializable<ListTag> {
    public static final String LIGHT_NBT_KEY = "neoforge:aux_lights";
    private static final String LEVEL_ERROR_MSG = "Light level must be in range 0-%d".formatted(LightEngine.MAX_LEVEL);

    private final LevelChunk owner;
    private final Map<BlockPos, Integer> lights = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public AuxiliaryLightManager(LevelChunk owner) {
        this.owner = owner;
    }

    /**
     * Set the light value at the given position to the given value
     */
    public void setLightAt(BlockPos pos, int value) {
        Preconditions.checkArgument(value >= 0 && value <= LightEngine.MAX_LEVEL, LEVEL_ERROR_MSG);
        Integer oldValue;
        if (value > 0) {
            oldValue = lights.put(pos, value);
        } else {
            oldValue = lights.remove(pos);
        }
        if (Objects.requireNonNullElse(oldValue, 0) != value) {
            owner.getLevel().getChunkSource().getLightEngine().checkBlock(pos);
            owner.setUnsaved(true);
        }
    }

    /**
     * Remove the light value at the given position
     */
    public void removeLightAt(BlockPos pos) {
        Integer oldValue = lights.remove(pos);
        if (oldValue != null) {
            owner.getLevel().getChunkSource().getLightEngine().checkBlock(pos);
            owner.setUnsaved(true);
        }
    }

    /**
     * {@return the light value at the given position or 0 if none is present}
     */
    public int getLightAt(BlockPos pos) {
        return lights.getOrDefault(pos, 0);
    }

    @Override
    @ApiStatus.Internal
    public ListTag serializeNBT() {
        if (lights.isEmpty()) {
            return null;
        }

        ListTag list = new ListTag();
        lights.forEach((pos, light) -> {
            CompoundTag tag = new CompoundTag();
            tag.putLong("pos", pos.asLong());
            tag.putByte("level", light.byteValue());
            list.add(tag);
        });
        return list;
    }

    @Override
    @ApiStatus.Internal
    public void deserializeNBT(ListTag list) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            lights.put(BlockPos.of(tag.getLong("pos")), (int) tag.getByte("level"));
        }
    }

    @ApiStatus.Internal
    public Packet<?> sendLightDataTo(ServerPlayer player, ClientboundLevelChunkWithLightPacket chunkPacket) {
        if (lights.isEmpty() || !player.connection.isConnected(AuxiliaryLightDataPayload.ID)) {
            return chunkPacket;
        }
        return new ClientboundBundlePacket(List.of(chunkPacket, new ClientboundCustomPayloadPacket(
                new AuxiliaryLightDataPayload(owner.getPos(), Map.copyOf(lights)))));
    }

    @ApiStatus.Internal
    public void handleLightDataSync(Map<BlockPos, Integer> lights) {
        this.lights.putAll(lights);
    }
}
