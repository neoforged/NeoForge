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
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

public class AttachmentHolderCodec<P extends IAttachmentHolder> implements Codec<AttachmentHolder<P>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AttachmentHolder<P> holder;

    public AttachmentHolderCodec(AttachmentHolder<P> holder) {
        this.holder = holder;
    }

    @Override
    public <T> DataResult<Pair<AttachmentHolder<P>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getMap(input).flatMap(map -> parseMap(ops, input, map));
    }

    private <T> DataResult<Pair<AttachmentHolder<P>, T>> parseMap(DynamicOps<T> ops, T input, MapLike<T> map) {
        final var entries = new IdentityHashMap<AttachmentType<?>, Object>(4);
        final Stream.Builder<Pair<T, T>> failed = Stream.builder();

        final DataResult<Unit> finalResult = map.entries().reduce(
                DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                (result, entry) -> parseEntry(result, ops, entry, entries, failed),
                (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2));

        final var holder = new AttachmentHolder<>(this.holder.parent);
        holder.getAttachmentMap().putAll(entries);
        final var pair = Pair.of(holder, input);
        final T errors = ops.createMap(failed.build());

        return finalResult.map(ignored -> pair).setPartial(pair).mapError(error -> error + " missed input: " + errors);
    }

    private <T> DataResult<Unit> parseEntry(DataResult<Unit> result, DynamicOps<T> ops, Pair<T, T> input,
            IdentityHashMap<AttachmentType<?>, Object> entries, Stream.Builder<Pair<T, T>> failed) {
        final var keyResult = ResourceLocation.CODEC.parse(ops, input.getFirst());
        final var attrType = keyResult.map(NeoForgeRegistries.ATTACHMENT_TYPES::get).getOrThrow();
        final var valueResult = keyResult
                .map(attKey -> parseSingleEntry(ops, input, attrType))
                .map(Function.identity());

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

    private <T, Att> Att parseSingleEntry(DynamicOps<T> ops, Pair<T, T> input, AttachmentType<Att> attrType) {
        var val = input.getSecond();
        var data = attrType.codec.parse(ops, val).getOrThrow();
        attrType.postDeserialize.accept(holder.parent, data);
        return data;
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
    public <T> DataResult<T> encode(AttachmentHolder<P> input, DynamicOps<T> ops, T prefix) {
        final RecordBuilder<T> mapBuilder = ops.mapBuilder();
        if (input.hasAttachments() && input.attachments != null) {
            input.attachments.forEach((attType, att) -> encodeAttachment(ops, mapBuilder, attType, att));
        }
        return mapBuilder.build(prefix);
    }
}
