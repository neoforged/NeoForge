/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handlers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.TierSortingRegistry;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import net.neoforged.neoforge.common.world.LevelChunkAuxiliaryLightManager;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.network.ConfigSync;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;
import net.neoforged.neoforge.network.payload.AdvancedContainerSetDataPayload;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import net.neoforged.neoforge.network.payload.AuxiliaryLightDataPayload;
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

    private final Set<ResourceLocation> toSynchronize = Sets.newConcurrentHashSet();
    private final Map<ResourceLocation, RegistrySnapshot> synchronizedRegistries = Maps.newConcurrentMap();

    private ClientPayloadHandler() {}

    public void handle(FrozenRegistryPayload payload, IPayloadContext context) {
        synchronizedRegistries.put(payload.registryName(), payload.snapshot());
        toSynchronize.remove(payload.registryName());
    }

    public void handle(FrozenRegistrySyncStartPayload payload, IPayloadContext context) {
        this.toSynchronize.addAll(payload.toAccess());
        this.synchronizedRegistries.clear();
    }

    public void handle(FrozenRegistrySyncCompletedPayload payload, IPayloadContext context) {
        if (!this.toSynchronize.isEmpty()) {
            context.disconnect(Component.translatable("neoforge.network.registries.sync.missing", this.toSynchronize.stream().map(Object::toString).collect(Collectors.joining(", "))));
            return;
        }

        context.enqueueWork(() -> {
            try {
                //This method normally returns missing entries, but we just accept what the server send us and ignore the rest.
                Set<ResourceKey<?>> keysUnknownToClient = RegistryManager.applySnapshot(synchronizedRegistries, false, false);
                if (!keysUnknownToClient.isEmpty()) {
                    context.disconnect(Component.translatable("neoforge.network.registries.sync.server-with-unknown-keys", keysUnknownToClient.stream().map(Object::toString).collect(Collectors.joining(", "))));
                    return;
                }

                this.toSynchronize.clear();
                this.synchronizedRegistries.clear();
            } catch (Throwable t) {
                context.disconnect(Component.translatable("neoforge.network.registries.sync.failed", t.getMessage()));
            }
        }).thenAccept(v -> {
            context.reply(new FrozenRegistrySyncCompletedPayload());
        });
    }

    public void handle(ConfigFilePayload payload, IPayloadContext context) {
        ConfigSync.INSTANCE.receiveSyncedConfig(payload.contents(), payload.fileName());
    }

    public void handle(TierSortingRegistryPayload payload, IPayloadContext context) {
        TierSortingRegistry.handleSync(payload, context);
    }

    @SuppressWarnings("resource")
    public void handle(AdvancedAddEntityPayload advancedAddEntityPayload, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    try {
                        Entity entity = Objects.requireNonNull(Minecraft.getInstance().level).getEntity(advancedAddEntityPayload.entityId());
                        if (entity instanceof IEntityWithComplexSpawn entityAdditionalSpawnData) {
                            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(advancedAddEntityPayload.customPayload()));
                            try {
                                entityAdditionalSpawnData.readSpawnData(buf);
                            } finally {
                                buf.release();
                            }
                        }
                    } catch (Throwable t) {
                        context.disconnect(Component.translatable("neoforge.network.advanced_add_entity.failed", t.getMessage()));
                    }
                });
    }

    public void handle(AdvancedOpenScreenPayload msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(msg.additionalData()));
            try {
                createMenuScreen(msg.name(), msg.menuType(), msg.windowId(), buf);
            } catch (Throwable t) {
                context.disconnect(Component.translatable("neoforge.network.advanced_open_screen.failed", t.getMessage()));
            } finally {
                buf.release();
            }
        });
    }

    private static <T extends AbstractContainerMenu> void createMenuScreen(Component name, MenuType<T> menuType, int windowId, FriendlyByteBuf buf) {
        Minecraft mc = Minecraft.getInstance();
        MenuScreens.getScreenFactory(menuType, mc, windowId, name).ifPresent(f -> {
            Screen s = f.create(menuType.create(windowId, mc.player.getInventory(), buf), mc.player.getInventory(), name);
            mc.player.containerMenu = ((MenuAccess<?>) s).getMenu();
            mc.setScreen(s);
        });
    }

    public void handle(AuxiliaryLightDataPayload msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null) return;

                AuxiliaryLightManager lightManager = mc.level.getAuxLightManager(msg.pos());
                if (lightManager instanceof LevelChunkAuxiliaryLightManager manager) {
                    manager.handleLightDataSync(msg.entries());
                }
            } catch (Throwable t) {
                context.disconnect(Component.translatable("neoforge.network.aux_light_data.failed", msg.pos(), t.getMessage()));
            }
        });
    }

    public void handle(AdvancedContainerSetDataPayload msg, IPayloadContext context) {
        context.handle(msg.toVanillaPacket());
    }
}
