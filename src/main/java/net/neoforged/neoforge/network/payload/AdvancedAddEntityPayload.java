/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.entity.IEntityAdditionalSpawnData;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * Payload that can be sent from the server to the client to add an entity to the world, with custom data.
 *
 * @param typeId        The type of the entity to add.
 * @param entityId      The id of the entity to add.
 * @param uuid          The uuid of the entity to add.
 * @param posX          The x position of the entity to add.
 * @param posY          The y position of the entity to add.
 * @param posZ          The z position of the entity to add.
 * @param pitch         The pitch of the entity to add.
 * @param yaw           The yaw of the entity to add.
 * @param headYaw       The head yaw of the entity to add.
 * @param velX          The x velocity of the entity to add.
 * @param velY          The y velocity of the entity to add.
 * @param velZ          The z velocity of the entity to add.
 * @param customPayload The custom data of the entity to add.
 */
@ApiStatus.Internal
public record AdvancedAddEntityPayload(
        EntityType<?> typeId,
        int entityId,
        UUID uuid,
        double posX,
        double posY,
        double posZ,
        byte pitch,
        byte yaw,
        byte headYaw,
        int velX,
        int velY,
        int velZ,
        byte[] customPayload) implements CustomPacketPayload {

    /**
     * The id of this payload.
     */
    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "advanced_add_entity");

    public AdvancedAddEntityPayload(FriendlyByteBuf buf) {
        this(
                buf.readById(BuiltInRegistries.ENTITY_TYPE),
                buf.readVarInt(),
                buf.readUUID(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readByte(),
                buf.readByte(),
                buf.readByte(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readByteArray());
    }

    public AdvancedAddEntityPayload(Entity e) {
        this(
                e.getType(),
                e.getId(),
                e.getUUID(),
                e.getX(),
                e.getY(),
                e.getZ(),
                (byte) Mth.floor(e.getXRot() * 256.0F / 360.0F),
                (byte) Mth.floor(e.getYRot() * 256.0F / 360.0F),
                (byte) (e.getYHeadRot() * 256.0F / 360.0F),
                (int) (Mth.clamp(e.getDeltaMovement().x, -3.9D, 3.9D) * 8000.0D),
                (int) (Mth.clamp(e.getDeltaMovement().y, -3.9D, 3.9D) * 8000.0D),
                (int) (Mth.clamp(e.getDeltaMovement().z, -3.9D, 3.9D) * 8000.0D),
                writeCustomData(e));
    }

    private static byte[] writeCustomData(final Entity entity) {
        if (!(entity instanceof IEntityAdditionalSpawnData additionalSpawnData)) {
            return new byte[0];
        }
        
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            additionalSpawnData.writeSpawnData(buf);
            return buf.array();
        } finally {
            buf.release();
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeId(BuiltInRegistries.ENTITY_TYPE, typeId());
        buffer.writeVarInt(entityId());
        buffer.writeUUID(uuid());
        buffer.writeDouble(posX());
        buffer.writeDouble(posY());
        buffer.writeDouble(posZ());
        buffer.writeByte(pitch());
        buffer.writeByte(yaw());
        buffer.writeByte(headYaw());
        buffer.writeVarInt(velX());
        buffer.writeVarInt(velY());
        buffer.writeVarInt(velZ());
        buffer.writeByteArray(customPayload());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
