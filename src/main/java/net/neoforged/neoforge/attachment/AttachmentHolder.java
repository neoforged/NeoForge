/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
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
public final class AttachmentHolder implements IAttachmentHolder {
    public static final String ATTACHMENTS_NBT_KEY = "neoforge:attachments";
    private static final Logger LOGGER = LogUtils.getLogger();

    private void validateAttachmentType(AttachmentType<?> type) {
        Objects.requireNonNull(type);
        if (FMLLoader.isProduction()) return;

        if (!NeoForgeRegistries.ATTACHMENT_TYPES.containsValue(type)) {
            throw new IllegalArgumentException("Data attachment type with default value " + type.defaultValueSupplier.apply(this) + " must be registered!");
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
            ret = type.defaultValueSupplier.apply(this);
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
                AttachmentHolder.CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), this)
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

    public static final Codec<AttachmentHolder> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<AttachmentHolder, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input).flatMap(map -> parseMap(ops, input, map));
        }

        private <T> DataResult<Pair<AttachmentHolder, T>> parseMap(DynamicOps<T> ops, T input, MapLike<T> map) {
            final var entries = new IdentityHashMap<AttachmentType<?>, Object>(4);
            final Stream.Builder<Pair<T, T>> failed = Stream.builder();

            final DataResult<Unit> finalResult = map.entries().reduce(
                    DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                    (result, entry) -> parseEntry(result, ops, entry, entries, failed),
                    (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2));

            final var holder = new AttachmentHolder();
            holder.attachments.putAll(entries);
            final var pair = Pair.of(holder, input);
            final T errors = ops.createMap(failed.build());

            return finalResult.map(ignored -> pair).setPartial(pair).mapError(error -> error + " missed input: " + errors);
        }

        private <T> DataResult<Unit> parseEntry(DataResult<Unit> result, DynamicOps<T> ops, Pair<T, T> input,
                IdentityHashMap<AttachmentType<?>, Object> entries, Stream.Builder<Pair<T, T>> failed) {
            final var keyResult = ResourceLocation.CODEC.parse(ops, input.getFirst());
            final var attrType = keyResult.map(NeoForgeRegistries.ATTACHMENT_TYPES::get).getOrThrow();
            final var valueResult = keyResult.map(attKey -> {
                var val = input.getSecond();
                return attrType.codec.parse(ops, val);
            }).map(Function.identity());

            final var entryResult = keyResult.apply2stable(Pair::of, valueResult);

            final var entry = entryResult.resultOrPartial();
            if (entry.isPresent()) {
                final var key = entry.get().getFirst();
                final var value = entry.get().getSecond();
                if (entries.putIfAbsent(attrType, value) != null) {
                    failed.add(input);
                    return result.apply2stable((u, p) -> u, DataResult.error(() -> "Duplicate entry for key: '" + key + "'"));
                }
            }
            if (entryResult.isError()) {
                LOGGER.error("Failed to deserialize data attachment {}. Skipping.", attrType);
                failed.add(input);
            }

            return result.apply2stable((u, p) -> u, entryResult);
        }

        private <T, AT> void encodeAttachment(DynamicOps<T> ops, RecordBuilder<T> mapBuilder,
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
                input.attachments.forEach((attType, att) -> encodeAttachment(ops, mapBuilder, attType, att));
            }
            return mapBuilder.build(prefix);
        }
    };
}
