/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.payload.SyncAttachmentsPayload;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.registries.callback.AddCallback;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@EventBusSubscriber(modid = NeoForgeVersion.MOD_ID)
public final class AttachmentInternals {
    /**
     * Copy some attachments to another holder.
     */
    private static <H extends AttachmentHolder> void copyAttachments(HolderLookup.Provider provider, H from, H to, Predicate<AttachmentType<?>> filter) {
        if (from.attachments == null) {
            return;
        }
        for (var entry : from.attachments.entrySet()) {
            AttachmentType<?> type = entry.getKey();
            if (type.serializer == null) {
                continue;
            }
            @SuppressWarnings("unchecked")
            var copyHandler = (IAttachmentCopyHandler<Object>) type.copyHandler;
            if (filter.test(type)) {
                Object copy = copyHandler.copy(entry.getValue(), to.getExposedHolder(), provider);
                if (copy != null) {
                    to.getAttachmentMap().put(type, copy);
                }
            }
        }
    }

    public static void copyChunkAttachmentsOnPromotion(HolderLookup.Provider provider, AttachmentHolder.AsField from, AttachmentHolder.AsField to) {
        copyAttachments(provider, from, to, type -> true);
    }

    /**
     * Do not call directly, use {@link IEntityExtension#copyAttachmentsFrom(Entity, boolean)}.
     */
    public static void copyEntityAttachments(Entity from, Entity to, boolean isDeath) {
        copyAttachments(from.registryAccess(), from, to, isDeath ? type -> type.copyOnDeath : type -> true);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().copyAttachmentsFrom(event.getOriginal(), event.isWasDeath());
    }

    @SubscribeEvent
    public static void onLivingConvert(LivingConversionEvent.Post event) {
        event.getOutcome().copyAttachmentsFrom(event.getEntity(), true);
    }

    /**
     * Contains all entries added to {@link NeoForgeRegistries#ATTACHMENT_TYPES} with a sync handler.
     * Should never be registered against directly.
     */
    public static final Registry<AttachmentType<?>> SYNCED_ATTACHMENT_TYPES = new RegistryBuilder<>(
            ResourceKey.<AttachmentType<?>>createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "synced_attachment_types")))
            .sync(true)
            .create();

    public static final AddCallback<AttachmentType<?>> ATTACHMENT_TYPE_ADD_CALLBACK = (registry, id, key, value) -> {
        if (value.syncHandler != null) {
            Registry.register(SYNCED_ATTACHMENT_TYPES, key.location(), value);
        }
    };

    public static <T> void syncEntityAttachment(Entity entity, AttachmentType<T> type, T value, AttachmentSyncReason reason) {
        if (type.syncHandler == null || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (var player : serverLevel.players()) {
            List<AttachmentType<?>> syncedTypes = new ArrayList<>(1);
            var data = FriendlyByteBufUtil.writeCustomData(buf -> {
                int indexBefore = buf.writerIndex();
                type.syncHandler.write(buf, value, player, reason);
                if (indexBefore < buf.writerIndex()) {
                    // Actually wrote something
                    syncedTypes.add(type);
                }
            }, entity.registryAccess());
            if (!syncedTypes.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new SyncAttachmentsPayload(new SyncAttachmentsPayload.EntityTarget(entity.getId()), syncedTypes, data));
            }
        }
    }

    @Nullable
    private static SyncAttachmentsPayload syncEntityAttachments(Entity entity, ServerPlayer to, AttachmentSyncReason reason) {
        var holder = (AttachmentHolder) entity;
        if (holder.attachments == null) {
            return null;
        }
        List<AttachmentType<?>> syncedTypes = new ArrayList<>();
        var data = FriendlyByteBufUtil.writeCustomData(buf -> {
            for (var entry : holder.attachments.entrySet()) {
                AttachmentType<?> type = entry.getKey();
                @SuppressWarnings("unchecked")
                var syncHandler = (IAttachmentSyncHandler<Object>) type.syncHandler;
                if (syncHandler != null) {
                    int indexBefore = buf.writerIndex();
                    syncHandler.write(buf, entry.getValue(), to, reason);
                    if (indexBefore < buf.writerIndex()) {
                        // Actually wrote something
                        syncedTypes.add(type);
                    }
                }
            }
        }, entity.registryAccess());
        return new SyncAttachmentsPayload(new SyncAttachmentsPayload.EntityTarget(entity.getId()), syncedTypes, data);
    }

    public static void sendEntityPairingData(Entity entity, ServerPlayer to, Consumer<Packet<? super ClientGamePacketListener>> packetConsumer) {
        var packet = syncEntityAttachments(entity, to, AttachmentSyncReason.NEW_ENTITY);
        if (packet != null) {
            packetConsumer.accept(packet.toVanillaClientbound());
        }
    }

    public static void receiveSyncedDataAttachments(AttachmentHolder holder, RegistryAccess registryAccess, List<AttachmentType<?>> types, byte[] bytes) {
        var buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), registryAccess, ConnectionType.NEOFORGE);
        try {
            for (var type : types) {
                @SuppressWarnings("unchecked")
                var syncHandler = (IAttachmentSyncHandler<Object>) type.syncHandler;
                if (syncHandler == null) {
                    throw new IllegalArgumentException("Received synced attachment type without a sync handler registered: " + NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type));
                }
                // TODO: need to be careful that the right holder is passed! (when delegating!)
                var result = syncHandler.read(holder.getExposedHolder(), buf);
                if (result == null) {
                    if (holder.attachments != null) {
                        holder.attachments.remove(type);
                    }
                } else {
                    holder.getAttachmentMap().put(type, result);
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException("Encountered exception when reading synced data attachments: " + types, exception);
        } finally {
            buf.release();
        }
    }

    private AttachmentInternals() {}
}
