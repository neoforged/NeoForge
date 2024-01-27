/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * Payload that can be sent from the server to the client to add an entity to the world, with custom data.
 *
 * @param entityId      The id of the entity to add.
 * @param customPayload The custom data of the entity to add.
 */
@ApiStatus.Internal
public record AdvancedAddEntityPayload(
        int entityId,
        byte[] customPayload) implements CustomPacketPayload {

    /**
     * The id of this payload.
     */
    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "advanced_add_entity");

    public AdvancedAddEntityPayload(FriendlyByteBuf buf) {
        this(
                buf.readVarInt(),
                buf.readByteArray());
    }

    public AdvancedAddEntityPayload(Entity e) {
        this(
                e.getId(),
                writeCustomData(e));
    }

    private static byte[] writeCustomData(final Entity entity) {
        if (!(entity instanceof IEntityWithComplexSpawn additionalSpawnData)) {
            return new byte[0];
        }

        return FriendlyByteBufUtil.writeCustomData(additionalSpawnData::writeSpawnData);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId());
        buffer.writeByteArray(customPayload());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
