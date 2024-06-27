/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import static net.minecraft.network.codec.ByteBufCodecs.tagCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public class AttachmentCodecs {
    /**
     * Instructs the attachment serializer that this is a codec for the attachment holder instance
     * it is performing operations on.
     *
     * @param holder The containing holder type; ie Entity, LevelChunk, etc.
     * @return A chainable codec-building instance.
     * @param <O>
     * @param <TOwner>
     */
    public static <O, TOwner extends IAttachmentHolder> RecordCodecBuilder<O, TOwner> holder(Class<TOwner> holder) {
        return ExtraCodecs.retrieveContext(ops -> {
            if (ops instanceof AttachmentOps<?, ?> dops) {
                if (holder.isInstance(dops.parent))
                    return DataResult.success(holder.cast(dops.parent));

                return DataResult.error(() -> "Wrong ops passed to function: Not the correct holder type for this attachment.");
            }

            return DataResult.error(() -> "Wrong ops passed to function: Not a data attachment ops");
        }).forGetter(o -> null);
    }

    /**
     * Instructs the attachment serializer that this is a stream codec for the attachment holder
     * instance it is performing operations on.
     *
     * @param holder    The containing holder type; ie Entity, LevelChunk, etc.
     * @param <THolder> The type holding attachment data.
     */
    public static <T, THolder extends IAttachmentHolder<THolder>> StreamCodec<AttachmentFriendlyByteBuf<THolder>, T> streamHolder() {
        return new AttachmentCodecs.StreamUnitCodec<T, THolder>();
    }

    public static <T, THolder extends IAttachmentHolder<?>> StreamCodec<AttachmentFriendlyByteBuf<THolder>, T> streamCodecFromCodec(final Codec<T> attachmentCodec) {
        final StreamCodec<ByteBuf, Tag> streamcodec = tagCodec(NbtAccounter::unlimitedHeap);
        return new StreamCodec<>() {
            public T decode(AttachmentFriendlyByteBuf<THolder> buffer) {
                Tag tag = streamcodec.decode(buffer);
                final var attachmentOps = AttachmentOps
                        .create(buffer.registryAccess(), NbtOps.INSTANCE, buffer.attachmentHolder);

                return attachmentCodec.parse(attachmentOps, tag)
                        .getOrThrow(error -> new DecoderException("Failed to decode: " + error + " " + tag));
            }

            public void encode(AttachmentFriendlyByteBuf<THolder> buffer, T instance) {
                final var attachmentOps = AttachmentOps
                        .create(buffer.registryAccess(), NbtOps.INSTANCE, buffer.attachmentHolder);

                Tag tag = attachmentCodec.encodeStart(attachmentOps, instance)
                        .getOrThrow(error -> new EncoderException("Failed to encode: " + error + " " + instance));

                streamcodec.encode(buffer, tag);
            }
        };
    }

    record StreamUnitCodec<T, THolder extends IAttachmentHolder<THolder>>() implements StreamCodec<AttachmentFriendlyByteBuf<THolder>, T> {
        public T decode(AttachmentFriendlyByteBuf<THolder> buf) {
            return (T) buf.attachmentHolder.parent();
        }

        public void encode(AttachmentFriendlyByteBuf<THolder> buffer, T instance) {}
    }
}
