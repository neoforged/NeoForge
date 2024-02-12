/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Mod.EventBusSubscriber(modid = NeoForgeVersion.MOD_ID)
public final class AttachmentInternals {
    /**
     * Marks a stack that has attachments and an empty NBT tag ({}).
     * If this marker is absent, we set the tag back to null after reading the attachments.
     */
    private static final String EMPTY_TAG_KEY = "neoforge:empty";

    @Nullable
    public static CompoundTag addAttachmentsToTag(@Nullable CompoundTag tag, ItemStack stack, boolean fullCopy) {
        // Store all serializable attachments as an nbt subtag
        CompoundTag attachmentsTag = stack.serializeAttachments();
        if (attachmentsTag != null) {
            if (tag == null)
                tag = new CompoundTag();
            else {
                tag = tag.copy();
                if (tag.isEmpty()) // Make sure we can differentiate between null and empty.
                    tag.putBoolean(EMPTY_TAG_KEY, true);
            }
            tag.put(AttachmentHolder.ATTACHMENTS_NBT_KEY, attachmentsTag);
        } else if (fullCopy && tag != null)
            tag = tag.copy();
        return tag;
    }

    /**
     * Perform the inverse operation of {@link #addAttachmentsToTag} on stack reception.
     */
    public static ItemStack reconstructItemStack(Item item, int count, @Nullable CompoundTag tag) {
        ItemStack itemstack;
        if (tag != null && tag.contains(AttachmentHolder.ATTACHMENTS_NBT_KEY, Tag.TAG_COMPOUND)) {
            // Read serialized caps
            itemstack = new ItemStack(item, count, tag.getCompound(AttachmentHolder.ATTACHMENTS_NBT_KEY));
            tag = cleanTag(tag);
        } else {
            itemstack = new ItemStack(item, count);
        }
        itemstack.setTag(tag);
        return itemstack;
    }

    /**
     * Clean tag of its contained attachments (as set by {@link #addAttachmentsToTag}).
     */
    @Nullable
    public static CompoundTag cleanTag(CompoundTag tag) {
        tag.remove(AttachmentHolder.ATTACHMENTS_NBT_KEY);
        // If the tag is now empty and the empty marker is absent, replace by null.
        if (tag.contains(EMPTY_TAG_KEY))
            tag.remove(EMPTY_TAG_KEY);
        else if (tag.isEmpty())
            tag = null;
        return tag;
    }

    /**
     * Copy some attachments to another holder.
     */
    private static <H extends AttachmentHolder> void copyAttachments(H from, H to, Predicate<AttachmentType<?>> filter) {
        if (from.attachments == null) {
            return;
        }
        for (var entry : from.attachments.entrySet()) {
            AttachmentType<?> type = entry.getKey();
            @SuppressWarnings("unchecked")
            var cloner = (IAttachmentCloner<Object>) type.cloner;
            if (filter.test(type)) {
                Object copy = cloner.copy(to.getExposedHolder(), entry.getValue());
                if (copy != null) {
                    to.getAttachmentMap().put(type, copy);
                }
            }
        }
    }

    public static void copyStackAttachments(ItemStack from, ItemStack to) {
        copyAttachments(from, to, type -> true);
    }

    public static void copyChunkAttachmentsOnPromotion(AttachmentHolder.AsField from, AttachmentHolder.AsField to) {
        copyAttachments(from, to, type -> true);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        copyAttachments(event.getOriginal(), event.getEntity(), event.isWasDeath() ? type -> type.copyOnDeath : type -> true);
    }

    @SubscribeEvent
    public static void onLivingConvert(LivingConversionEvent.Post event) {
        copyAttachments(event.getEntity(), event.getOutcome(), type -> type.copyOnDeath);
    }

    private AttachmentInternals() {}
}
