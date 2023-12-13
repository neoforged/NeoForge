package net.neoforged.neoforge.network.payload;

import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record AdvancedAddEntityPayload(int typeId,
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
    
    public static final ResourceLocation ID = new ResourceLocation("neoforge", "advanced_add_entity");
    
    public AdvancedAddEntityPayload(FriendlyByteBuf buf) {
        this(
                buf.readVarInt(),
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
                buf.readByteArray()
        );
    }
    
    public AdvancedAddEntityPayload(Entity e) {
        this(
                BuiltInRegistries.ENTITY_TYPE.getId(e.getType()),
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
                writeCustomData(e)
        );
    }
    
    private static byte[] writeCustomData(final Entity entity) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        if (entity instanceof IEntityAdditionalSpawnData additionalSpawnData) {
            additionalSpawnData.writeSpawnData(buf);
        }
        
        final byte[] payload = buf.array();
        buf.release();
        return payload;
    }
    
    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeVarInt(typeId);
        buffer.writeVarInt(entityId);
        buffer.writeUUID(uuid);
        buffer.writeDouble(posX);
        buffer.writeDouble(posY);
        buffer.writeDouble(posZ);
        buffer.writeByte(pitch);
        buffer.writeByte(yaw);
        buffer.writeByte(headYaw);
        buffer.writeVarInt(velX);
        buffer.writeVarInt(velY);
        buffer.writeVarInt(velZ);
        buffer.writeBytes(customPayload);
    }
    
    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
