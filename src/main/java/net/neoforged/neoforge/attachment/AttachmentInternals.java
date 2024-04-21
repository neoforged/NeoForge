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
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

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

    private AttachmentInternals() {}
}
