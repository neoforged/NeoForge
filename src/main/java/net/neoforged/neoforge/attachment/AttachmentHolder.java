/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.logging.LogUtils;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.fml.loading.FMLEnvironment;
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
    private static final boolean IN_DEV = !FMLEnvironment.isProduction();
    private static final Logger LOGGER = LogUtils.getLogger();

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
            syncData(type);
        }
        return ret;
    }

    @Override
    @Nullable
    public <T> T getExistingDataOrNull(AttachmentType<T> type) {
        validateAttachmentType(type);
        if (attachments == null) {
            return null;
        }
        return (T) this.attachments.get(type);
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T setData(AttachmentType<T> type, T data) {
        validateAttachmentType(type);
        Objects.requireNonNull(data);
        var previousData = (T) getAttachmentMap().put(type, data);
        syncData(type);
        return previousData;
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T removeData(AttachmentType<T> type) {
        validateAttachmentType(type);
        if (attachments == null) {
            return null;
        }
        var previousData = (T) attachments.remove(type);
        syncData(type);
        return previousData;
    }

    /**
     * Writes the serializable attachments to a tag.
     */
    public final void serializeAttachments(ValueOutput tag) {
        if (attachments == null) return;
        for (var entry : attachments.entrySet()) {
            var type = entry.getKey();
            var key = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type);
            if (type.serializer != null) {
                try {
                    var serialized = tag.child(key.toString());
                    boolean doSerialise = ((IAttachmentSerializer) type.serializer).write(entry.getValue(), serialized);
                    if (!doSerialise) {
                        tag.discard(key.toString());
                    }
                } catch (Exception exception) {
                    LOGGER.error("Failed to serialize data attachment {}. Skipping.", key, exception);
                }
            }
        }
    }

    /**
     * Reads serializable attachments from a tag previously created via {@link #serializeAttachments(ValueOutput)}.
     *
     * <p>This does not trigger {@link IAttachmentHolder#syncData syncing} of the deserialized attachments.
     */
    protected final void deserializeAttachments(ValueInput input) {
        for (var key : input.keySet()) {
            // Use tryParse to not discard valid attachment type keys, even if there is a malformed key.
            ResourceLocation keyLocation = ResourceLocation.tryParse(key);
            if (keyLocation == null) {
                LOGGER.error("Encountered invalid data attachment key {}. Skipping.", key);
                continue;
            }

            var type = NeoForgeRegistries.ATTACHMENT_TYPES.getValue(keyLocation);
            if (type == null || type.serializer == null) {
                LOGGER.error("Encountered unknown or non-serializable data attachment {}. Skipping.", key);
                continue;
            }

            try {
                getAttachmentMap().put(type, type.serializer.read(getExposedHolder(), input.childOrEmpty(key)));
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

        public void deserializeInternal(HolderLookup.Provider provider, ValueInput tag) {
            deserializeAttachments(tag);
        }

        @Override
        public void syncData(AttachmentType<?> type) {
            exposedHolder.syncData(type);
        }
    }
}
