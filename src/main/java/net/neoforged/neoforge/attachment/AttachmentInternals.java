/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@EventBusSubscriber(modid = NeoForgeMod.MOD_ID)
public final class AttachmentInternals {
    /**
     * Copy some attachments to another holder.
     */
    private static <H extends AttachmentHolder> void copyAttachments(HolderLookup.Provider provider, H from, H to, Predicate<AttachmentType<?>> filter) {
        final var copyable = from.attachmentDataStorage()
                .storedTypes()
                .filter(t -> t.serializer != null)
                .toList();

        for (var type : copyable) {
            @SuppressWarnings("unchecked")
            var copyHandler = (IAttachmentCopyHandler<Object>) type.copyHandler;
            if (filter.test(type)) {
                final var v = from.getExistingDataOrNull(type);
                Object copy = copyHandler.copy(v, to, provider);
                if (copy != null) {
                    to.putDataNoSync(type, copy);
                }
            }
        }
    }

    public static void copyChunkAttachmentsOnPromotion(HolderLookup.Provider provider, AttachmentHolder from, AttachmentHolder to) {
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

    private AttachmentInternals() {}
}
