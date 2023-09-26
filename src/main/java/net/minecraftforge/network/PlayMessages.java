/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.network;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.attachment.AttachmentType;
import net.minecraftforge.registries.attachment.AttachmentTypeKey;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PlayMessages
{
    /**
     * Used to spawn a custom entity without the same restrictions as
     * {@link ClientboundAddEntityPacket}
     * <p>
     * To customize how your entity is created clientside (instead of using the default factory provided to the
     * {@link EntityType})
     * see {@link EntityType.Builder#setCustomClientFactory}.
     */
    public static class SpawnEntity
    {
        private final Entity entity;
        private final int typeId;
        private final int entityId;
        private final UUID uuid;
        private final double posX, posY, posZ;
        private final byte pitch, yaw, headYaw;
        private final int velX, velY, velZ;
        private final FriendlyByteBuf buf;

        SpawnEntity(Entity e)
        {
            this.entity = e;
            this.typeId = BuiltInRegistries.ENTITY_TYPE.getId(e.getType()); //TODO: Codecs
            this.entityId = e.getId();
            this.uuid = e.getUUID();
            this.posX = e.getX();
            this.posY = e.getY();
            this.posZ = e.getZ();
            this.pitch = (byte) Mth.floor(e.getXRot() * 256.0F / 360.0F);
            this.yaw = (byte) Mth.floor(e.getYRot() * 256.0F / 360.0F);
            this.headYaw = (byte) (e.getYHeadRot() * 256.0F / 360.0F);
            Vec3 vec3d = e.getDeltaMovement();
            double d1 = Mth.clamp(vec3d.x, -3.9D, 3.9D);
            double d2 = Mth.clamp(vec3d.y, -3.9D, 3.9D);
            double d3 = Mth.clamp(vec3d.z, -3.9D, 3.9D);
            this.velX = (int) (d1 * 8000.0D);
            this.velY = (int) (d2 * 8000.0D);
            this.velZ = (int) (d3 * 8000.0D);
            this.buf = null;
        }

        private SpawnEntity(int typeId, int entityId, UUID uuid, double posX, double posY, double posZ, byte pitch, byte yaw, byte headYaw, int velX, int velY, int velZ, FriendlyByteBuf buf)
        {
            this.entity = null;
            this.typeId = typeId;
            this.entityId = entityId;
            this.uuid = uuid;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.pitch = pitch;
            this.yaw = yaw;
            this.headYaw = headYaw;
            this.velX = velX;
            this.velY = velY;
            this.velZ = velZ;
            this.buf = buf;
        }

        public static void encode(SpawnEntity msg, FriendlyByteBuf buf)
        {
            buf.writeVarInt(msg.typeId);
            buf.writeInt(msg.entityId);
            buf.writeLong(msg.uuid.getMostSignificantBits());
            buf.writeLong(msg.uuid.getLeastSignificantBits());
            buf.writeDouble(msg.posX);
            buf.writeDouble(msg.posY);
            buf.writeDouble(msg.posZ);
            buf.writeByte(msg.pitch);
            buf.writeByte(msg.yaw);
            buf.writeByte(msg.headYaw);
            buf.writeShort(msg.velX);
            buf.writeShort(msg.velY);
            buf.writeShort(msg.velZ);
            if (msg.entity instanceof IEntityAdditionalSpawnData entityAdditionalSpawnData)
            {
                final FriendlyByteBuf spawnDataBuffer = new FriendlyByteBuf(Unpooled.buffer());

                entityAdditionalSpawnData.writeSpawnData(spawnDataBuffer);

                buf.writeVarInt(spawnDataBuffer.readableBytes());
                buf.writeBytes(spawnDataBuffer);

                spawnDataBuffer.release();
            } else
            {
                buf.writeVarInt(0);
            }
        }

        public static SpawnEntity decode(FriendlyByteBuf buf)
        {
            return new SpawnEntity(buf.readVarInt(), buf.readInt(), new UUID(buf.readLong(), buf.readLong()), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readByte(), buf.readByte(), buf.readByte(), buf.readShort(), buf.readShort(), buf.readShort(), readSpawnDataPacket(buf));
        }

        private static FriendlyByteBuf readSpawnDataPacket(FriendlyByteBuf buf)
        {
            final int count = buf.readVarInt();
            if (count > 0)
            {
                final FriendlyByteBuf spawnDataBuffer = new FriendlyByteBuf(Unpooled.buffer());
                spawnDataBuffer.writeBytes(buf, count);
                return spawnDataBuffer;
            }

            return new FriendlyByteBuf(Unpooled.buffer());
        }

        public static void handle(SpawnEntity msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                try
                {
                    EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.byId(msg.typeId);
                    Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide());
                    Entity e = world.map(w -> type.customClientSpawn(msg, w)).orElse(null);
                    if (e == null)
                    {
                        return;
                    }

                    /*
                     * Sets the postiion on the client, Mirrors what
                     * Entity#recreateFromPacket and LivingEntity#recreateFromPacket does.
                     */
                    e.syncPacketPositionCodec(msg.posX, msg.posY, msg.posZ);
                    e.absMoveTo(msg.posX, msg.posY, msg.posZ, (msg.yaw * 360) / 256.0F, (msg.pitch * 360) / 256.0F);
                    e.setYHeadRot((msg.headYaw * 360) / 256.0F);
                    e.setYBodyRot((msg.headYaw * 360) / 256.0F);

                    e.setId(msg.entityId);
                    e.setUUID(msg.uuid);
                    world.filter(ClientLevel.class::isInstance).ifPresent(w -> ((ClientLevel) w).putNonPlayerEntity(msg.entityId, e));
                    e.lerpMotion(msg.velX / 8000.0, msg.velY / 8000.0, msg.velZ / 8000.0);
                    if (e instanceof IEntityAdditionalSpawnData entityAdditionalSpawnData)
                    {
                        entityAdditionalSpawnData.readSpawnData(msg.buf);
                    }
                } finally
                {
                    msg.buf.release();
                }
            });
            ctx.get().setPacketHandled(true);
        }

        public Entity getEntity()
        {
            return entity;
        }

        public int getTypeId()
        {
            return typeId;
        }

        public int getEntityId()
        {
            return entityId;
        }

        public UUID getUuid()
        {
            return uuid;
        }

        public double getPosX()
        {
            return posX;
        }

        public double getPosY()
        {
            return posY;
        }

        public double getPosZ()
        {
            return posZ;
        }

        public byte getPitch()
        {
            return pitch;
        }

        public byte getYaw()
        {
            return yaw;
        }

        public byte getHeadYaw()
        {
            return headYaw;
        }

        public int getVelX()
        {
            return velX;
        }

        public int getVelY()
        {
            return velY;
        }

        public int getVelZ()
        {
            return velZ;
        }

        public FriendlyByteBuf getAdditionalData()
        {
            return buf;
        }
    }

    public static class OpenContainer
    {
        private final int id;
        private final int windowId;
        private final Component name;
        private final FriendlyByteBuf additionalData;

        OpenContainer(MenuType<?> id, int windowId, Component name, FriendlyByteBuf additionalData)
        {
            this(BuiltInRegistries.MENU.getId(id), windowId, name, additionalData);
        }

        private OpenContainer(int id, int windowId, Component name, FriendlyByteBuf additionalData)
        {
            this.id = id;
            this.windowId = windowId;
            this.name = name;
            this.additionalData = additionalData;
        }

        public static void encode(OpenContainer msg, FriendlyByteBuf buf)
        {
            buf.writeVarInt(msg.id);
            buf.writeVarInt(msg.windowId);
            buf.writeComponent(msg.name);
            buf.writeByteArray(msg.additionalData.readByteArray());
        }

        public static OpenContainer decode(FriendlyByteBuf buf)
        {
            return new OpenContainer(buf.readVarInt(), buf.readVarInt(), buf.readComponent(), new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray(32600))));
        }

        public static void handle(OpenContainer msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                try
                {
                    MenuScreens.getScreenFactory(msg.getType(), Minecraft.getInstance(), msg.getWindowId(), msg.getName()).ifPresent(f -> {
                        AbstractContainerMenu c = msg.getType().create(msg.getWindowId(), Minecraft.getInstance().player.getInventory(), msg.getAdditionalData());

                        @SuppressWarnings("unchecked") Screen s = ((MenuScreens.ScreenConstructor<AbstractContainerMenu, ?>) f).create(c, Minecraft.getInstance().player.getInventory(), msg.getName());
                        Minecraft.getInstance().player.containerMenu = ((MenuAccess<?>) s).getMenu();
                        Minecraft.getInstance().setScreen(s);
                    });
                } finally
                {
                    msg.getAdditionalData().release();
                }

            });
            ctx.get().setPacketHandled(true);
        }

        public final MenuType<?> getType()
        {
            return BuiltInRegistries.MENU.byId(this.id);
        }

        public int getWindowId()
        {
            return windowId;
        }

        public Component getName()
        {
            return name;
        }

        public FriendlyByteBuf getAdditionalData()
        {
            return additionalData;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public record SyncAttachments<R>(ResourceKey<Registry<R>> registry, Supplier<Map<AttachmentType<?, R>, Map<Object, ?>>> payload)
    {
        public void encode(FriendlyByteBuf buf)
        {
            buf.writeResourceKey(registry);
            writeMap(buf, payload.get(), (buf1, key) -> buf1.writeResourceLocation(key.key().getId()), (buf1, key, objectMap) ->
            {
                buf1.writeMap(objectMap, (buf2, attachKey) ->
                {
                    if (attachKey instanceof ResourceKey<?> res)
                    {
                        buf2.writeUtf(res.location().toString());
                    }
                    else
                    {
                        buf2.writeUtf("#" + ((TagKey) attachKey).location());
                    }
                }, (buf2, value) -> buf2.writeJsonWithCodec((Codec) key.networkCodec(), value));
            });
        }

        private static <K, V> void writeMap(FriendlyByteBuf buf, Map<K, V> pMap, FriendlyByteBuf.Writer<K> pKeyWriter, TriConsumer<FriendlyByteBuf, K, V> pValueWriter)
        {
            buf.writeVarInt(pMap.size());
            pMap.forEach((key, value) ->
            {
                pKeyWriter.accept(buf, key);
                pValueWriter.accept(buf, key, value);
            });
        }

        public static SyncAttachments<?> decode(FriendlyByteBuf buf)
        {
            if (!FMLLoader.getDist().isClient())
            {
                throw new UnsupportedOperationException("Cannot deserialize SyncAttachments packet on server!");
            }
            return decodeClient(buf);
        }

        private static SyncAttachments<?> decodeClient(FriendlyByteBuf buf) {
            final var key = buf.readResourceKey(BuiltInRegistries.REGISTRY.key());
            final FriendlyByteBuf newBuf = new FriendlyByteBuf(Unpooled.copiedBuffer(buf));
            return new SyncAttachments(
                    key,
                    () -> decode((ResourceKey)key, newBuf)
            );
        }

        private static <R> Map<AttachmentType<?, R>, Map<Object, ?>> decode(ResourceKey<Registry<R>> registryKey, FriendlyByteBuf buf)
        {
            final ClientLevel level = Minecraft.getInstance().level;
            final Registry<R> registry = level.registryAccess().registryOrThrow(registryKey);
            return readMap(buf, Maps::newHashMapWithExpectedSize, buf1 -> registry.getAttachmentHolder().getAttachmentTypes().get(AttachmentTypeKey.get(buf1.readResourceLocation())), (buf1, key) -> buf1.readMap(
                    buf2 ->
                    {
                        final String rkey = buf1.readUtf();
                        return rkey.startsWith("#") ? TagKey.create(registryKey, new ResourceLocation(rkey.substring(1))) : ResourceKey.create(registryKey, new ResourceLocation(rkey));
                    },
                    buf2 -> buf2.readJsonWithCodec(key.networkCodec())
            ));
        }

        private static <K, V, M extends Map<K, V>> M readMap(FriendlyByteBuf buf, IntFunction<M> mapFactory, FriendlyByteBuf.Reader<K> keyReader, BiFunction<FriendlyByteBuf, K, V> valueReader)
        {
            final int size = buf.readVarInt();
            final M map = mapFactory.apply(size);

            for (int i = 0; i < size; i++)
            {
                final K k = keyReader.apply(buf);
                final V v = valueReader.apply(buf, k);
                map.put(k, v);
            }

            return map;
        }

        public static <R> void handle(SyncAttachments<R> msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() ->
            {
                final ClientLevel level = Minecraft.getInstance().level;
                final Registry<R> registry = level.registryAccess().registryOrThrow(msg.registry);
                final Map<AttachmentTypeKey<?>, Map<Object, ?>> attachments = new HashMap<>();
                msg.payload.get().forEach((t, v) -> attachments.put(t.key(), v));
                attachments.keySet().removeIf(key ->
                {
                   final var onClientAttachment = registry.getAttachmentHolder().getAttachmentTypes().get(key);
                    return onClientAttachment == null; // If it's null, it means it's not forcibly synced, so clients don't HAVE to have the data. otherwise they wouldn't have been able to connect
                });
                registry.getAttachmentHolder().getAttachmentTypes().forEach((key, attachment) ->
                {
                    if (!attachments.containsKey(key) && attachment.forciblySynced())
                    {
                        throw new IllegalStateException("Forcibly synced attachment " + key.getId() + " on registry " + registry.key() + " is present on the client, but not on the server!");
                    }
                });
                registry.getAttachmentHolder().bindAttachments(attachments);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
