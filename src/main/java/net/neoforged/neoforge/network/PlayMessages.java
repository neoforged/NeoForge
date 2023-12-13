/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.entity.IEntityAdditionalSpawnData;

public class PlayMessages {
    /**
     * Used to spawn a custom entity without the same restrictions as
     * {@link ClientboundAddEntityPacket}
     * <p>
     * To customize how your entity is created clientside (instead of using the default factory provided to the
     * {@link EntityType})
     * see {@link EntityType.Builder#setCustomClientFactory}.
     */
    public static class SpawnEntity {
        private final Entity entity;
        private final int typeId;
        private final int entityId;
        private final UUID uuid;
        private final double posX, posY, posZ;
        private final byte pitch, yaw, headYaw;
        private final int velX, velY, velZ;
        private final FriendlyByteBuf buf;


        public static boolean handle(SpawnEntity msg, NetworkEvent.Context ctx) {
            try {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.byId(msg.typeId);
                Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(ctx.getDirection().getReceptionSide());
                Entity e = world.map(w -> type.customClientSpawn(msg, w)).orElse(null);
                if (e == null) {
                    return true;
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
                world.filter(ClientLevel.class::isInstance).ifPresent(w -> ((ClientLevel) w).addEntity(e));
                e.lerpMotion(msg.velX / 8000.0, msg.velY / 8000.0, msg.velZ / 8000.0);
                if (e instanceof IEntityAdditionalSpawnData entityAdditionalSpawnData) {
                    entityAdditionalSpawnData.readSpawnData(msg.buf);
                }
            } finally {
                msg.buf.release();
            }
            return true;
        }

        public Entity getEntity() {
            return entity;
        }

        public int getTypeId() {
            return typeId;
        }

        public int getEntityId() {
            return entityId;
        }

        public UUID getUuid() {
            return uuid;
        }

        public double getPosX() {
            return posX;
        }

        public double getPosY() {
            return posY;
        }

        public double getPosZ() {
            return posZ;
        }

        public byte getPitch() {
            return pitch;
        }

        public byte getYaw() {
            return yaw;
        }

        public byte getHeadYaw() {
            return headYaw;
        }

        public int getVelX() {
            return velX;
        }

        public int getVelY() {
            return velY;
        }

        public int getVelZ() {
            return velZ;
        }

        public FriendlyByteBuf getAdditionalData() {
            return buf;
        }
    }

    public static class OpenContainer {
        private final int id;
        private final int windowId;
        private final Component name;
        private final FriendlyByteBuf additionalData;

        OpenContainer(MenuType<?> id, int windowId, Component name, FriendlyByteBuf additionalData) {
            this(BuiltInRegistries.MENU.getId(id), windowId, name, additionalData);
        }

        private OpenContainer(int id, int windowId, Component name, FriendlyByteBuf additionalData) {
            this.id = id;
            this.windowId = windowId;
            this.name = name;
            this.additionalData = additionalData;
        }

        public static void encode(OpenContainer msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.id);
            buf.writeVarInt(msg.windowId);
            buf.writeComponent(msg.name);
            buf.writeByteArray(msg.additionalData.readByteArray());
        }

        public static OpenContainer decode(FriendlyByteBuf buf) {
            return new OpenContainer(buf.readVarInt(), buf.readVarInt(), buf.readComponent(), new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray(32600))));
        }

        public static boolean handle(OpenContainer msg, NetworkEvent.Context ctx) {
            try {
                MenuScreens.getScreenFactory(msg.getType(), Minecraft.getInstance(), msg.getWindowId(), msg.getName()).ifPresent(f -> {
                    AbstractContainerMenu c = msg.getType().create(msg.getWindowId(), Minecraft.getInstance().player.getInventory(), msg.getAdditionalData());

                    @SuppressWarnings("unchecked")
                    Screen s = ((MenuScreens.ScreenConstructor<AbstractContainerMenu, ?>) f).create(c, Minecraft.getInstance().player.getInventory(), msg.getName());
                    Minecraft.getInstance().player.containerMenu = ((MenuAccess<?>) s).getMenu();
                    Minecraft.getInstance().setScreen(s);
                });
            } finally {
                msg.getAdditionalData().release();
            }
            return true;
        }

        public final MenuType<?> getType() {
            return BuiltInRegistries.MENU.byId(this.id);
        }

        public int getWindowId() {
            return windowId;
        }

        public Component getName() {
            return name;
        }

        public FriendlyByteBuf getAdditionalData() {
            return additionalData;
        }
    }
}
