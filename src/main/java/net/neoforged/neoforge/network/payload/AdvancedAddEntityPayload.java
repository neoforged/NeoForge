/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.ApiStatus;

/**
 * Payload that can be sent from the server to the client to add an entity to the world, with custom data.
 *
 * @param entityId      The id of the entity to add.
 * @param customPayload The custom data of the entity to add.
 */
@ApiStatus.Internal
public record AdvancedAddEntityPayload(int entityId, byte[] customPayload) implements CustomPacketPayload {
    public static final Type<AdvancedAddEntityPayload> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "advanced_add_entity"));
    public static final StreamCodec<FriendlyByteBuf, AdvancedAddEntityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            AdvancedAddEntityPayload::entityId,
            NeoForgeStreamCodecs.UNBOUNDED_BYTE_ARRAY,
            AdvancedAddEntityPayload::customPayload,
            AdvancedAddEntityPayload::new);

    public AdvancedAddEntityPayload(Entity e) {
        this(e.getId(), writeCustomData(e));
    }

    private static byte[] writeCustomData(final Entity entity) {
        if (!(entity instanceof IEntityWithComplexSpawn additionalSpawnData)) {
            return new byte[0];
        }

        return FriendlyByteBufUtil.writeCustomData(additionalSpawnData::writeSpawnData, entity.registryAccess());
    }

    @Override
    public Type<AdvancedAddEntityPayload> type() {
        return TYPE;
    }
}
