/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.payload.SyncEntityAttachmentsPayload;
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

    @Nullable
    private static SyncEntityAttachmentsPayload syncEntityAttachments(Entity entity, ServerPlayer to, AttachmentSyncReason reason) {
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
        return new SyncEntityAttachmentsPayload(entity.getId(), syncedTypes, data);
    }

    public static void sendEntityPairingData(Entity entity, ServerPlayer to, Consumer<Packet<? super ClientGamePacketListener>> packetConsumer) {
        var packet = syncEntityAttachments(entity, to, AttachmentSyncReason.NEW_ENTITY);
        if (packet != null) {
            packetConsumer.accept(packet.toVanillaClientbound());
        }
    }

    private AttachmentInternals() {}
}
