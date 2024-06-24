/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Implementation class for objects that can hold data attachments.
 * For the user-facing methods, see {@link IAttachmentHolder}.
 */
public final class AttachmentHolder<T extends IAttachmentHolder> implements IAttachmentHolder {
    public static final String ATTACHMENTS_NBT_KEY = "neoforge:attachments";
    private static final Logger LOGGER = LogUtils.getLogger();

    public final Codec<AttachmentHolder<T>> CODEC = new AttachmentHolderCodec<T>(this);

    @Nullable
    T parent;

    @Nullable
    Map<AttachmentType<?>, Object> attachments = null;

    AttachmentHolder() {}

    public AttachmentHolder(T parent) {
        this.parent = parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }

    private void validateAttachmentType(AttachmentType<?> type) {
        Objects.requireNonNull(type);
        if (FMLLoader.isProduction()) return;

        if (!NeoForgeRegistries.ATTACHMENT_TYPES.containsValue(type)) {
            throw new IllegalArgumentException("Data attachment type with default value " + type.defaultValueSupplier.apply(parent) + " must be registered!");
        }
    }

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
            ret = type.defaultValueSupplier.apply(parent);
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

    @Override
    public Stream<AttachmentType<?>> existingDataTypes() {
        return attachments.keySet().stream();
    }

    public final CompoundTag serializeAttachments(HolderLookup.Provider lookup) {
        return (CompoundTag) Objects.requireNonNullElseGet(
                CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), this)
                        .resultOrPartial() // TODO Log errors
                        .orElseGet(CompoundTag::new),
                CompoundTag::new);
    }

    /**
     * Reads serializable attachments from a tag previously created via {@link #CODEC}.
     */
    public final void deserializeAttachments(HolderLookup.Provider lookup, CompoundTag tag) {
        CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE), tag)
                .ifSuccess(parsedData -> {
                    assert parsedData.attachments != null;
                    this.attachments.putAll(parsedData.attachments);
                });
    }
}
