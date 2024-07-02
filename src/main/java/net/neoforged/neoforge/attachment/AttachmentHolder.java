/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Implementation class for objects that can hold data attachments.
 * For the user-facing methods, see {@link IAttachmentHolder}.
 */
public final class AttachmentHolder<T extends IAttachmentHolder<T>> implements IAttachmentHolder<T> {
    public static final String ATTACHMENTS_NBT_KEY = "neoforge:attachments";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    final T parent;

    @Nullable
    Map<AttachmentType<?>, Object> attachments = null;

    public final Codec<AttachmentHolder<T>> CODEC;

    public AttachmentHolder(T parent) {
        this.parent = parent;

        Class<T> parentClass = (Class<T>) parent.getClass();
        this.CODEC = RecordCodecBuilder.create(inst -> inst.group(
                AttachmentCodecs.holder(parentClass),
                AttachmentHolderMapCodec.INSTANCE.forGetter(x -> x.getAttachmentMap())).apply(inst, AttachmentHolder::new));
    }

    private AttachmentHolder(T parent, Map<AttachmentType<?>, Object> attachments) {
        this(parent);
        this.getAttachmentMap().putAll(attachments);
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
    public T parent() {
        return parent;
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

    private final AttachmentOps<Tag, T> makeNbtOps(HolderLookup.Provider lookup) {
        return AttachmentOps.create(lookup, NbtOps.INSTANCE, this.parent);
    }

    public final CompoundTag serializeAttachments(HolderLookup.Provider lookup) {
        return (CompoundTag) Objects.requireNonNullElseGet(
                CODEC.encodeStart(makeNbtOps(lookup), this)
                        .resultOrPartial() // TODO Log errors
                        .orElseGet(CompoundTag::new),
                CompoundTag::new);
    }

    /**
     * Reads serializable attachments from a tag previously created via {@link #CODEC}.
     */
    public final void deserializeAttachments(HolderLookup.Provider lookup, CompoundTag tag) {
        CODEC.parse(makeNbtOps(lookup), tag).ifSuccess(parsedData -> {
            assert parsedData.attachments != null;
            this.getAttachmentMap().putAll(parsedData.attachments);
        });
    }
}
