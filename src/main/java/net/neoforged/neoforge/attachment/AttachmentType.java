/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a data attachment type: some data that can be added to any object implementing {@link IAttachmentHolder}.
 *
 * <p>Data attachment types must be registered to {@link NeoForgeRegistries.Keys#ATTACHMENT_TYPES the registry}.
 *
 * <h3>{@link BlockEntity}-exclusive behavior:</h3>
 * <ul>
 * <li>Modifications to attachments should be followed by a call to {@link BlockEntity#setChanged()}.</li>
 * </ul>
 * <h3>{@link Entity}-exclusive behavior:</h3>
 * <ul>
 * <li>Serializable entity attachments are not copied on death by default (but they are copied when returning from the end).</li>
 * <li>Serializable entity attachments can opt into copying on death via {@link Builder#copyOnDeath()}.</li>
 * </ul>
 * <h3>{@link ItemStack}-exclusive behavior:</h3>
 * <ul>
 * <li>Serializable item stack attachments are synced between the server and the client.</li>
 * <li>Serializable item stack attachments are copied when an item stack is copied.</li>
 * <li>Serializable item stack attachments must match for item stack comparison to succeed.</li>
 * </ul>
 * <h3>{@link Level}-exclusive behavior:</h3>
 * <ul>
 * <li>(nothing)</li>
 * </ul>
 * <h3>{@link LevelChunk}-exclusive behavior:</h3>
 * <ul>
 * <li>Modifications to attachments should be followed by a call to {@link LevelChunk#setUnsaved(boolean)}.</li>
 * </ul>
 */
// TODO Future work: maybe add copy handlers for stacks and entities, to customize copying behavior (instead of serializing, copying the NBT, deserializing).
public final class AttachmentType<T> {
    final Function<IAttachmentHolder, T> defaultValueSupplier;
    @Nullable
    final IAttachmentSerializer<?, T> serializer;
    final boolean copyOnDeath;
    final IAttachmentComparator<T> comparator;

    private AttachmentType(Builder<T> builder) {
        this.defaultValueSupplier = builder.defaultValueSupplier;
        this.serializer = builder.serializer;
        this.copyOnDeath = builder.copyOnDeath;
        this.comparator = builder.comparator != null ? builder.comparator : defaultComparator(serializer);
    }

    private static <T> IAttachmentComparator<T> defaultComparator(IAttachmentSerializer<?, T> serializer) {
        if (serializer == null) {
            return (first, second) -> {
                throw new UnsupportedOperationException("Cannot compare non-serializable attachments");
            };
        }
        return (first, second) -> Objects.equals(serializer.write(first), serializer.write(second));
    }

    /**
     * Creates a builder for an attachment type.
     *
     * <p>See {@link #builder(Function)} for attachments that want to capture a reference to their holder.
     *
     * @param defaultValueSupplier A supplier for a new default value of this attachment type.
     */
    public static <T> Builder<T> builder(Supplier<T> defaultValueSupplier) {
        return builder(holder -> defaultValueSupplier.get());
    }

    /**
     * Creates a builder for an attachment type.
     *
     * <p>This overload allows capturing a reference to the {@link IAttachmentHolder} for the attachment.
     * To obtain a specific subtype, the holder can be cast.
     * If the holder is of the wrong type, the constructor should throw an exception.
     * See {@link #builder(Supplier)} for an overload that does not capture the holder.
     *
     * @param defaultValueConstructor A constructor for a new default value of this attachment type.
     */
    public static <T> Builder<T> builder(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return new Builder<>(defaultValueConstructor);
    }

    /**
     * Create a builder for an attachment type that uses {@link INBTSerializable} for serialization.
     * Other kinds of serialization can be implemented using {@link #builder(Supplier)} and {@link Builder#serialize(IAttachmentSerializer)}.
     *
     * <p>See {@link #serializable(Function)} for attachments that want to capture a reference to their holder.
     */
    public static <S extends Tag, T extends INBTSerializable<S>> Builder<T> serializable(Supplier<T> defaultValueSupplier) {
        return serializable(holder -> defaultValueSupplier.get());
    }

    /**
     * Create a builder for an attachment type that uses {@link INBTSerializable} for serialization.
     * Other kinds of serialization can be implemented using {@link #builder(Supplier)} and {@link Builder#serialize(IAttachmentSerializer)}.
     *
     * <p>This overload allows capturing a reference to the {@link IAttachmentHolder} for the attachment.
     * To obtain a specific subtype, the holder can be cast.
     * If the holder is of the wrong type, the constructor should throw an exception.
     * See {@link #serializable(Supplier)} for an overload that does not capture the holder.
     */
    public static <S extends Tag, T extends INBTSerializable<S>> Builder<T> serializable(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return builder(defaultValueConstructor).serialize(new IAttachmentSerializer<S, T>() {
            @Override
            public T read(IAttachmentHolder holder, S tag) {
                var ret = defaultValueConstructor.apply(holder);
                ret.deserializeNBT(tag);
                return ret;
            }

            @Nullable
            @Override
            public S write(T attachment) {
                return attachment.serializeNBT();
            }
        });
    }

    public static class Builder<T> {
        private final Function<IAttachmentHolder, T> defaultValueSupplier;
        @Nullable
        private IAttachmentSerializer<?, T> serializer;
        private boolean copyOnDeath;
        @Nullable
        private IAttachmentComparator<T> comparator;

        private Builder(Function<IAttachmentHolder, T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
        }

        /**
         * Requests that this attachment be persisted to disk (on the logical server side).
         *
         * @param serializer The serializer to use.
         */
        public Builder<T> serialize(IAttachmentSerializer<?, T> serializer) {
            Objects.requireNonNull(serializer);
            if (this.serializer != null)
                throw new IllegalStateException("Serializer already set");

            this.serializer = serializer;
            return this;
        }

        /**
         * Requests that this attachment be persisted to disk (on the logical server side), using a {@link Codec}.
         *
         * <p>Using a {@link Codec} to serialize attachments is discouraged for item stack attachments,
         * for performance reasons. Prefer one of the other options.
         *
         * <p>Codec-based attachments cannot capture a reference to their holder.
         *
         * @param codec The codec to use.
         */
        public Builder<T> serialize(Codec<T> codec) {
            return serialize(codec, Predicates.alwaysTrue());
        }

        /**
         * Requests that this attachment be persisted to disk (on the logical server side), using a {@link Codec}.
         *
         * <p>Using a {@link Codec} to serialize attachments is discouraged for item stack attachments,
         * for performance reasons. Prefer one of the other options.
         *
         * <p>Codec-based attachments cannot capture a reference to their holder.
         *
         * @param codec           The codec to use.
         * @param shouldSerialize A check that determines whether serialization of the attachment should occur.
         */
        public Builder<T> serialize(Codec<T> codec, Predicate<? super T> shouldSerialize) {
            Objects.requireNonNull(codec);
            // TODO: better error handling
            return serialize(new IAttachmentSerializer<>() {
                @Override
                public T read(IAttachmentHolder holder, Tag tag) {
                    return codec.parse(NbtOps.INSTANCE, tag).result().get();
                }

                @Nullable
                @Override
                public Tag write(T attachment) {
                    return shouldSerialize.test(attachment) ? codec.encodeStart(NbtOps.INSTANCE, attachment).result().get() : null;
                }
            });
        }

        /**
         * Requests that this attachment be persisted when a player respawns or when a living entity is converted.
         */
        public Builder<T> copyOnDeath() {
            if (this.serializer == null)
                throw new IllegalStateException("copyOnDeath requires a serializer");
            this.copyOnDeath = true;
            return this;
        }

        /**
         * Overrides the comparator for this attachment type.
         *
         * <p>The default comparator checks for equality of the serialized NBT tag:
         * {@code Objects.equals(serializer.write(first), serializer.write(second))}.
         *
         * <p>A comparator can only be provided for serializable attachments.
         */
        public Builder<T> comparator(IAttachmentComparator<T> comparator) {
            Objects.requireNonNull(comparator);
            // Check for serializer because only serializable attachments can be compared.
            if (this.serializer == null)
                throw new IllegalStateException("comparator requires a serializer");
            this.comparator = comparator;
            return this;
        }

        public AttachmentType<T> build() {
            return new AttachmentType<>(this);
        }
    }
}
