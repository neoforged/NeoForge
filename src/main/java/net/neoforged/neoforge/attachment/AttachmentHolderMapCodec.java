/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

public class AttachmentHolderMapCodec extends MapCodec<Map<AttachmentType<?>, Object>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final AttachmentHolderMapCodec INSTANCE = new AttachmentHolderMapCodec();

    @Override
    public <T> RecordBuilder<T> encode(Map<AttachmentType<?>, Object> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        input.forEach((attType, att) -> encodeAttachment(ops, prefix, attType, att));
        return prefix;
    }

    private <T, AT> void encodeAttachment(DynamicOps<T> ops, RecordBuilder<T> mapBuilder,
            AttachmentType<AT> attachment, Object attachmentValue) {
        try {
            //noinspection unchecked
            final AT attData = (AT) attachmentValue;
            if (attachment.shouldSerialize.test(attData) && attachment.codec != null) {
                final var attTypeKey = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(attachment);
                final var encoded = attachment.codec.encodeStart(ops, attData);
                mapBuilder.add(ResourceLocation.CODEC.encodeStart(ops, attTypeKey), encoded);
            }
        } catch (ClassCastException cce) {
            LOGGER.atError().setCause(cce).log("Encountered unknown or non-serializable data attachment {}. Skipping.", attachment);
        }
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.empty();
    }

    @Override
    public <T> DataResult<Map<AttachmentType<?>, Object>> decode(DynamicOps<T> ops, MapLike<T> map) {
        final var entries = new IdentityHashMap<AttachmentType<?>, Object>(4);
        final ImmutableSet.Builder<Pair<T, T>> failed = ImmutableSet.builder();

        map.entries().forEach(entry -> {
            final var attrType = NeoForgeRegistries.ATTACHMENT_TYPES.byNameCodec()
                    .parse(ops, entry.getFirst());

            final var valueResult = attrType.getOrThrow().codec
                    .parse(ops, entry.getSecond());

            final DataResult<Pair<AttachmentType<?>, Object>> entryResult = attrType.apply2stable(Pair::of, valueResult);
            final Optional<Pair<AttachmentType<?>, Object>> eOpt = entryResult.resultOrPartial();
            eOpt.ifPresent(pair -> {
                final Object existingValue = entries.putIfAbsent(pair.getFirst(), pair.getSecond());
                if (existingValue != null) {
                    failed.add(entry);
                }
            });

            if (entryResult.isError()) {
                failed.add(entry);
            }
        });

        final var errors = failed.build();
        if (errors.isEmpty())
            return DataResult.success(entries);

        return DataResult.error(() -> "Some attachment entries did not decode correctly.", entries);
    }
}
