/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.RecordBuilder;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Implementation class for objects that can hold data attachments.
 * For the user-facing methods, see {@link IAttachmentHolder}.
 */
public abstract class AttachmentHolder implements IAttachmentHolder {
    public static final String ATTACHMENTS_NBT_KEY = "neoforge:attachments";
    private static final boolean IN_DEV = !FMLLoader.isProduction();
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Encoder<AttachmentHolder> ATTACHMENTS_ENCODER = new Encoder<>() {
        private <T, AT> void encodeAttachment(DynamicOps<T> ops, RecordBuilder<T> mapBuilder, AttachmentHolder input,
                AttachmentType<AT> attachment, Object attachmentValue) {
            try {
                //noinspection unchecked
                final AT attData = (AT) attachmentValue;
                if (attachment.shouldSerialize.test(attData) && attachment.codec != null) {
                    final var encoded = attachment.codec.encodeStart(ops, attData);

                    final var attTypeKey = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(attachment);
                    mapBuilder.add(ResourceLocation.CODEC.encodeStart(ops, attTypeKey), encoded);
                }
            } catch (ClassCastException cce) {
                LOGGER.atError().setCause(cce).log("Encountered unknown or non-serializable data attachment {}. Skipping.", attachment);
            }
        }

        @Override
        public <T> DataResult<T> encode(AttachmentHolder input, DynamicOps<T> ops, T prefix) {
            final RecordBuilder<T> mapBuilder = ops.mapBuilder();
            if (input.hasAttachments() && input.attachments != null) {
                input.attachments.forEach((attType, att) -> encodeAttachment(ops, mapBuilder, input, attType, att));
            }
            return mapBuilder.build(prefix);
        }
    };

    private void validateAttachmentType(AttachmentType<?> type) {
        Objects.requireNonNull(type);
        if (!IN_DEV) return;

        if (!NeoForgeRegistries.ATTACHMENT_TYPES.containsValue(type)) {
            throw new IllegalArgumentException("Data attachment type with default value " + type.defaultValueSupplier.apply(getExposedHolder()) + " must be registered!");
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

    /**
     * Returns the attachment holder that is exposed to the user.
     * This is the same as {@code this} for most cases,
     * but when using {@link AsField} it is the field holder.
     */
    IAttachmentHolder getExposedHolder() {
        return this;
    }

    @Override
    public final boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    @Override
    public final boolean hasData(AttachmentType<?> type) {
        validateAttachmentType(type);
        return attachments != null && attachments.containsKey(type);
    }

    @Override
    public final <T> T getData(AttachmentType<T> type) {
        validateAttachmentType(type);
        T ret = (T) getAttachmentMap().get(type);
        if (ret == null) {
            ret = type.defaultValueSupplier.apply(getExposedHolder());
            attachments.put(type, ret);
        }
        return ret;
    }

    @Override
    public <T> Optional<T> getExistingData(AttachmentType<T> type) {
        validateAttachmentType(type);
        if (attachments == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) this.attachments.get(type));
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T setData(AttachmentType<T> type, T data) {
        validateAttachmentType(type);
        Objects.requireNonNull(data);
        return (T) getAttachmentMap().put(type, data);
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T removeData(AttachmentType<T> type) {
        validateAttachmentType(type);
        if (attachments == null) {
            return null;
        }
        return (T) attachments.remove(type);
    }

    /**
     * Reads serializable attachments from a tag previously created via {@link #ATTACHMENTS_ENCODER}.
     */
    protected final void deserializeAttachments(HolderLookup.Provider lookup, CompoundTag tag) {
        for (var key : tag.getAllKeys()) {
            // Use tryParse to not discard valid attachment type keys, even if there is a malformed key.
            ResourceLocation keyLocation = ResourceLocation.tryParse(key);
            if (keyLocation == null) {
                LOGGER.error("Encountered invalid data attachment key {}. Skipping.", key);
                continue;
            }

            var type = NeoForgeRegistries.ATTACHMENT_TYPES.get(keyLocation);
            if (type == null || type.codec == null) {
                LOGGER.error("Encountered unknown or non-serializable data attachment {}. Skipping.", key);
                continue;
            }

            try {
                getAttachmentMap().put(type, type.codec.parse(lookup.createSerializationContext(NbtOps.INSTANCE), tag.get(key))
                        .resultOrPartial(LOGGER::error)
                        .orElseThrow());
            } catch (Exception exception) {
                LOGGER.error("Failed to deserialize data attachment {}. Skipping.", key, exception);
            }
        }
    }

    /**
     * Version of the {@link AttachmentHolder} that is suitable for storing in a field.
     * To be used when extending {@link AttachmentHolder} is not possible,
     * for example because the class already has a supertype.
     */
    public static class AsField extends AttachmentHolder {
        private final IAttachmentHolder exposedHolder;

        public AsField(IAttachmentHolder exposedHolder) {
            this.exposedHolder = exposedHolder;
        }

        @Override
        IAttachmentHolder getExposedHolder() {
            return exposedHolder;
        }

        public void deserializeInternal(HolderLookup.Provider provider, CompoundTag tag) {
            deserializeAttachments(provider, tag);
        }
    }
}
