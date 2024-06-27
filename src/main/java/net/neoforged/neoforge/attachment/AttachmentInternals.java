/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@EventBusSubscriber(modid = NeoForgeVersion.MOD_ID)
public final class AttachmentInternals {
    /**
     * Copy some attachments to another holder.
     */
	@ApiStatus.Internal
    public static <T extends IAttachmentHolder, H extends AttachmentHolder<T>> void copyAttachments(
            RegistryAccess registryAccess, IAttachmentHolder from, H to,
            PendingAttachmentCopy.CopyReason copyReason) {

        if (!from.hasAttachments()) {
            return;
        }

        var buffer = new AttachmentFriendlyByteBuf<>(Unpooled.buffer(), registryAccess, to);
        from.existingDataTypes()
				.filter(type -> type.copyHandler != null)
                .map(type -> createPendingCopy(type, from, to))
                .filter(pendingCopy -> pendingCopy.attachmentType().copyCheck.test(copyReason, pendingCopy))
                .filter(pendingCopy -> pendingCopy.attachmentType().copyHandler != null)
                .forEach(pendingCopy -> {
                    pendingCopy.attachmentType().copyHandler.encode(buffer, pendingCopy.data());

                    Object copied = pendingCopy.attachmentType().copyHandler.decode(buffer);
                    pendingCopy.newHost().getAttachmentMap().put(pendingCopy.attachmentType(), copied);
                });

        buffer.clear();
        buffer.release();
    }

    private static <D, H extends AttachmentHolder<?>> PendingAttachmentCopy<AttachmentType<D>, D, H> createPendingCopy(AttachmentType<?> type, IAttachmentHolder from, H to) {
        return new PendingAttachmentCopy<>((AttachmentType<D>) type, from, to, (D) from.getData(type));
    }

    public static void copyChunkAttachmentsOnPromotion(RegistryAccess registryAccess, IAttachmentHolder from, AttachmentHolder<?> to) {
        copyAttachments(registryAccess, from, to, PendingAttachmentCopy.CopyReason.CHUNK_PROMOTION);
    }

    /**
     * Do not call directly, use {@link IEntityExtension#copyAttachmentsFrom(Entity, boolean)}.
     */
    public static void copyEntityAttachments(Entity from, Entity to, PendingAttachmentCopy.CopyReason reason) {
        copyAttachments(from.registryAccess(), from, to.dataAttachments(), reason);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().copyAttachmentsFrom(event.getOriginal(), event.isWasDeath() ? PendingAttachmentCopy.CopyReason.DEATH : PendingAttachmentCopy.CopyReason.NOT_SPECIFIED);
    }

    @SubscribeEvent
    public static void onLivingConvert(LivingConversionEvent.Post event) {
        event.getOutcome().copyAttachmentsFrom(event.getEntity(), PendingAttachmentCopy.CopyReason.CONVERTED);
    }

    private AttachmentInternals() {}
}
