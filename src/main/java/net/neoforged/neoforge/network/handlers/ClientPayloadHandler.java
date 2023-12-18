/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handlers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.TierSortingRegistry;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.entity.IEntityAdditionalSpawnData;
import net.neoforged.neoforge.network.ConfigSync;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.network.payload.TierSortingRegistryPayload;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.RegistrySnapshot;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ClientPayloadHandler {

    private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();

    public static ClientPayloadHandler getInstance() {
        return INSTANCE;
    }

    private final Set<ResourceLocation> toSynchronize = Sets.newHashSet();
    private final Map<ResourceLocation, RegistrySnapshot> synchronizedRegistries = Maps.newHashMap();

    private ClientPayloadHandler() {}

    public void handle(ConfigurationPayloadContext context, FrozenRegistryPayload payload) {
        synchronizedRegistries.put(payload.registryName(), payload.snapshot());
        toSynchronize.remove(payload.registryName());
    }

    public void handle(ConfigurationPayloadContext context, FrozenRegistrySyncStartPayload payload) {
        this.toSynchronize.addAll(payload.toAccess());
        this.synchronizedRegistries.clear();
    }

    public void handle(ConfigurationPayloadContext context, FrozenRegistrySyncCompletedPayload payload) {
        if (!this.toSynchronize.isEmpty()) {
            context.packetHandler().disconnect(Component.translatable("neoforge.registries.sync.failed", this.toSynchronize.stream().map(Object::toString).collect(Collectors.joining(", "))));
            return;
        }

        //This method normally returns missing entries, but we just accept what the server send us and ignore the rest.
        RegistryManager.applySnapshot(synchronizedRegistries, false, false);

        this.toSynchronize.clear();
        this.synchronizedRegistries.clear();

        context.handler().send(new FrozenRegistrySyncCompletedPayload());
    }

    public void handle(ConfigurationPayloadContext context, ConfigFilePayload payload) {
        ConfigSync.INSTANCE.receiveSyncedConfig(payload.contents(), payload.fileName());
    }

    public void handle(ConfigurationPayloadContext context, TierSortingRegistryPayload payload) {
        TierSortingRegistry.handleSync(context, payload);
    }

    public void handle(PlayPayloadContext context, AdvancedAddEntityPayload msg) {
        Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(context.flow().getReceptionSide());
        Entity e = world.map(w -> msg.typeId().customClientSpawn(msg, w)).orElse(null);
        if (e == null) {
            return;
        }

        /*
         * Sets the postiion on the client, Mirrors what
         * Entity#recreateFromPacket and LivingEntity#recreateFromPacket does.
         */
        e.syncPacketPositionCodec(msg.posX(), msg.posY(), msg.posZ());
        e.absMoveTo(msg.posX(), msg.posY(), msg.posZ(), (msg.yaw() * 360) / 256.0F, (msg.pitch() * 360) / 256.0F);
        e.setYHeadRot((msg.headYaw() * 360) / 256.0F);
        e.setYBodyRot((msg.headYaw() * 360) / 256.0F);

        e.setId(msg.entityId());
        e.setUUID(msg.uuid());
        world.filter(ClientLevel.class::isInstance).ifPresent(w -> ((ClientLevel) w).addEntity(e));
        e.lerpMotion(msg.velX() / 8000.0, msg.velY() / 8000.0, msg.velZ() / 8000.0);
        if (e instanceof IEntityAdditionalSpawnData entityAdditionalSpawnData) {
            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(msg.customPayload()));
            try {
                entityAdditionalSpawnData.readSpawnData(buf);
            } finally {
                buf.release();
            }
        }
    }

    public void handle(PlayPayloadContext context, AdvancedOpenScreenPayload msg) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(msg.additionalData()));
        try {
            createMenuScreen(msg.name(), msg.menuType(), msg.windowId(), buf);
        } finally {
            buf.release();
        }
    }

    private static <T extends AbstractContainerMenu> void createMenuScreen(Component name, MenuType<T> menuType, int windowId, FriendlyByteBuf buf) {
        Minecraft mc = Minecraft.getInstance();
        MenuScreens.getScreenFactory(menuType, mc, windowId, name).ifPresent(f -> {
            Screen s = f.create(menuType.create(windowId, mc.player.getInventory(), buf), mc.player.getInventory(), name);
            mc.player.containerMenu = ((MenuAccess<?>) s).getMenu();
            mc.setScreen(s);
        });
    }
}
