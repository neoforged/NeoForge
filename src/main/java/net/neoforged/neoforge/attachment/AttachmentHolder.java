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
public abstract class AttachmentHolder<H extends IAttachmentHolder<H>> implements IAttachmentHolder<H> {
    public static final String ATTACHMENTS_NBT_KEY = "neoforge:attachments";
    private static final boolean IN_DEV = !FMLLoader.isProduction();

    private void validateAttachmentType(AttachmentType<? super H, ?> type) {
        Objects.requireNonNull(type);
        if (!IN_DEV) return;

        if (!NeoForgeRegistries.ATTACHMENT_TYPES.containsValue(type)) {
            throw new IllegalArgumentException("Data attachment type with default value " + type.defaultValueSupplier.apply(getExposedHolder()) + " must be registered!");
        }
    }

    final Map<AttachmentType<? super H, ?>, Object> attachments = new IdentityHashMap<>();

    /**
     * Returns the attachment holder that is exposed to the user.
     * This is the same as {@code this} for most cases,
     * but when using {@link AsField} it is the field holder.
     */
    H getExposedHolder() {
        return (H) this;
    }

    @Override
    public final boolean hasData(AttachmentType<? super H, ?> type) {
        validateAttachmentType(type);
        return attachments.containsKey(type);
    }

    @Override
    public final <T> T getData(AttachmentType<? super H, T> type) {
        validateAttachmentType(type);
        T ret = (T) attachments.get(type);
        if (ret == null) {
            ret = type.defaultValueSupplier.apply(getExposedHolder());
            attachments.put(type, ret);
        }
        return ret;
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T setData(AttachmentType<? super H, T> type, T data) {
        validateAttachmentType(type);
        Objects.requireNonNull(data);
        return (T) attachments.put(type, data);
    }

    /**
     * Writes the serializable attachments to a tag.
     * Returns {@code null} if there are no serializable attachments.
     */
    @Nullable
    public final CompoundTag serializeAttachments() {
        CompoundTag tag = null;
        for (var entry : attachments.entrySet()) {
            var type = entry.getKey();
            if (type.serializer != null) {
                if (tag == null)
                    tag = new CompoundTag();
                tag.put(NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type).toString(), ((IAttachmentSerializer<H, ?, Object>) type.serializer).write(entry.getValue()));
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
                    if (type.holderClass.isAssignableFrom(getExposedHolder().getClass())) {
                        attachments.put((AttachmentType<? super H, ?>) type, ((IAttachmentSerializer<H, Tag, ?>) type.serializer).read(getExposedHolder(), tag.get(key)));
                    }
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
    public static <H extends AttachmentHolder<H>> boolean areAttachmentsCompatible(H first, H second) {
        for (var entry : first.attachments.entrySet()) {
            AttachmentType<? super H, Object> type = (AttachmentType<? super H, Object>) entry.getKey();
            if (type.serializer != null) {
                var otherData = second.attachments.get(type);
                if (otherData == null)
                    // TODO: cache serialization of default value?
                    otherData = type.defaultValueSupplier.apply(second.getExposedHolder());
                if (!type.comparator.areCompatible(entry.getValue(), otherData))
                    return false;
            }
        }
        for (var entry : second.attachments.entrySet()) {
            AttachmentType<? super H, Object> type = (AttachmentType<? super H, Object>) entry.getKey();
            if (type.serializer != null) {
                var data = first.attachments.get(type);
                if (data != null)
                    continue; // already checked in the first loop
                data = type.defaultValueSupplier.apply(first.getExposedHolder());
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
    public static class AsField<H extends IAttachmentHolder<H>> extends AttachmentHolder<H> {
        private final H exposedHolder;

        public AsField(H exposedHolder) {
            this.exposedHolder = exposedHolder;
        }

        @Override
        H getExposedHolder() {
            return exposedHolder;
        }

        public void deserializeInternal(CompoundTag tag) {
            deserializeAttachments(tag);
        }
    }
}
