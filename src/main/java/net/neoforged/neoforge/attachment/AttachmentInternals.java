/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import io.netty.buffer.Unpooled;
import java.util.function.Predicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
    private static <H extends AttachmentHolder<?>> void copyAttachments(RegistryAccess registryAccess, IAttachmentHolder from, H to, Predicate<AttachmentType<?>> filter) {
        if (!from.hasAttachments()) {
            return;
        }

        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess, ConnectionType.NEOFORGE);
        from.existingDataTypes()
                .filter(filter)
                .filter(type -> type.copyHandler != null)
                .forEach(type -> {
                    copySingleAttachment(type, from, to, type.copyHandler, buffer);
                });

        buffer.clear();
        buffer.release();
    }

    @SuppressWarnings("unchecked")
    private static <D, H extends AttachmentHolder<?>> void copySingleAttachment(AttachmentType<?> type, IAttachmentHolder from, H to, StreamCodec<RegistryFriendlyByteBuf, D> copyHandler, RegistryFriendlyByteBuf buffer) {
        var data = (D) from.getData(type);
        copyHandler.encode(buffer, data);

        var copied = copyHandler.decode(buffer);
        to.getAttachmentMap().put(type, copied);
    }

    public static void copyChunkAttachmentsOnPromotion(RegistryAccess registryAccess, IAttachmentHolder from, AttachmentHolder<?> to) {
        copyAttachments(registryAccess, from, to, type -> true);
    }

    /**
     * Do not call directly, use {@link IEntityExtension#copyAttachmentsFrom(Entity, boolean)}.
     */
    public static void copyEntityAttachments(Entity from, Entity to, boolean isDeath) {
        copyAttachments(from.registryAccess(), from, (AttachmentHolder<Entity>) to.dataAttachments(), isDeath ? type -> type.copyOnDeath : type -> true);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().copyAttachmentsFrom(event.getOriginal(), event.isWasDeath());
    }

    @SubscribeEvent
    public static void onLivingConvert(LivingConversionEvent.Post event) {
        event.getOutcome().copyAttachmentsFrom(event.getEntity(), true);
    }

    private AttachmentInternals() {}
}
