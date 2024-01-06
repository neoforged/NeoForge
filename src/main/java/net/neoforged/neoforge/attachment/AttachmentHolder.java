/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation class for objects that can hold data attachments.
 * For the user-facing methods, see {@link IAttachmentHolder}.
 */
public abstract class AttachmentHolder implements IAttachmentHolder {
    public static final String ATTACHMENTS_NBT_KEY = "neoforge:attachments";
    private static final boolean IN_DEV = !FMLLoader.isProduction();

    private static void validateAttachmentType(AttachmentType<?> type) {
        Objects.requireNonNull(type);
        if (!IN_DEV) return;

        if (!NeoForgeRegistries.ATTACHMENT_TYPES.containsValue(type)) {
            throw new IllegalArgumentException("Data attachment type with default value " + type.defaultValueSupplier.get() + " must be registered!");
        }
    }

    @Nullable
    Map<AttachmentType<?>, Object> attachments = null;

    /**
     * Create the attachment map if it does not yet exist, or return the current map.
     */
    final Map<AttachmentType<?>, Object> getAttachmentMap() {
        if (attachments == null) {
            attachments = new IdentityHashMap<>(4);
        }
        return attachments;
    }

    @Override
    public final boolean hasData(AttachmentType<?> type) {
        validateAttachmentType(type);
        return attachments != null && attachments.containsKey(type);
    }

    @Override
    public final <T> T getData(AttachmentType<T> type) {
        validateAttachmentType(type);
        return (T) getAttachmentMap().computeIfAbsent(type, t -> Objects.requireNonNull(t.defaultValueSupplier.get()));
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T setData(AttachmentType<T> type, T data) {
        validateAttachmentType(type);
        Objects.requireNonNull(data);
        return (T) getAttachmentMap().put(type, data);
    }

    /**
     * Writes the serializable attachments to a tag.
     * Returns {@code null} if there are no serializable attachments.
     */
    @Nullable
    public final CompoundTag serializeAttachments() {
        if (attachments == null) {
            return null;
        }
        CompoundTag tag = null;
        for (var entry : attachments.entrySet()) {
            var type = entry.getKey();
            if (type.serializer != null) {
                if (tag == null)
                    tag = new CompoundTag();
                tag.put(NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type).toString(), ((IAttachmentSerializer<?, Object>) type.serializer).write(entry.getValue()));
            }
        }
        return tag;
    }

    /**
     * Reads serializable attachments from a tag previously created via {@link #serializeAttachments()}.
     */
    protected final void deserializeAttachments(CompoundTag tag) {
        for (var key : tag.getAllKeys()) {
            // Use tryParse to not discard valid attachment type keys, even if there is a malformed key.
            ResourceLocation keyLocation = ResourceLocation.tryParse(key);
            if (keyLocation != null) {
                var type = NeoForgeRegistries.ATTACHMENT_TYPES.get(keyLocation);
                if (type != null && type.serializer != null) {
                    getAttachmentMap().put(type, ((IAttachmentSerializer<Tag, ?>) type.serializer).read(tag.get(key)));
                }
            }
        }
    }

    /**
     * Checks if two attachment holders have compatible attachments,
     * i.e. if they have the same serialized form.
     *
     * <p>Same as calling {@code Objects.equals(first.serializeAttachments(), second.serializeAttachments())},
     * but implemented more efficiently.
     *
     * @return {@code true} if the attachments are compatible, {@code false} otherwise
     */
    public static <H extends AttachmentHolder> boolean areAttachmentsCompatible(H first, H second) {
        Map<AttachmentType<?>, Object> firstAttachments = first.attachments != null ? first.attachments : Map.of();
        Map<AttachmentType<?>, Object> secondAttachments = second.attachments != null ? second.attachments : Map.of();

        for (var entry : firstAttachments.entrySet()) {
            AttachmentType<Object> type = (AttachmentType<Object>) entry.getKey();
            if (type.serializer != null) {
                var otherData = secondAttachments.get(type);
                if (otherData == null)
                    // TODO: cache serialization of default value?
                    otherData = type.defaultValueSupplier.get();
                if (!type.comparator.areCompatible(entry.getValue(), otherData))
                    return false;
            }
        }
        for (var entry : secondAttachments.entrySet()) {
            AttachmentType<Object> type = (AttachmentType<Object>) entry.getKey();
            if (type.serializer != null) {
                var data = firstAttachments.get(type);
                if (data != null)
                    continue; // already checked in the first loop
                data = type.defaultValueSupplier.get();
                if (!type.comparator.areCompatible(entry.getValue(), data))
                    return false;
            }
        }
        return true;
    }

    /**
     * Version of the {@link AttachmentHolder} that is suitable for storing in a field.
     * To be used when extending {@link AttachmentHolder} is not possible,
     * for example because the class already has a supertype.
     */
    public static class AsField extends AttachmentHolder {
        public void deserializeInternal(CompoundTag tag) {
            deserializeAttachments(tag);
        }
    }
}
