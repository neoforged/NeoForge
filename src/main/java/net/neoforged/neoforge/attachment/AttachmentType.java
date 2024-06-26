/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.jodah.typetools.TypeResolver;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
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
 * <h3>{@link Level}-exclusive behavior:</h3>
 * <ul>
 * <li>(nothing)</li>
 * </ul>
 * <h3>{@link ChunkAccess}-exclusive behavior:</h3>
 * <ul>
 * <li>Modifications to attachments should be followed by a call to {@link ChunkAccess#setUnsaved(boolean)}.</li>
 * <li>Serializable attachments are copied from a {@link ProtoChunk} to a {@link LevelChunk} on promotion.</li>
 * </ul>
 */
public final class AttachmentType<T> {
    public final Class<T> dataType;
    final Function<IAttachmentHolder, T> defaultValueSupplier;
    @Nullable
    final Codec<T> codec;
    final Predicate<T> shouldSerialize;
    final boolean copyOnDeath;
    @Nullable
    final StreamCodec<RegistryFriendlyByteBuf, T> copyHandler;

    private <P extends IAttachmentHolder> AttachmentType(Builder<T, P> builder) {
        this.dataType = builder.dataType;
        this.defaultValueSupplier = (Function<IAttachmentHolder, T>) builder.defaultValueSupplier;
        this.shouldSerialize = builder.shouldSerialize;
        this.copyOnDeath = builder.copyOnDeath;

        if (builder.serializer != null) {
            final var deserializer = builder.deserializer != null ? builder.deserializer : Decoder.<T>error("invalid state -- no decoder provided");

            this.codec = Codec.of(builder.serializer, deserializer);
            this.copyHandler = builder.copyHandler != null ? builder.copyHandler : ByteBufCodecs.fromCodecWithRegistries(this.codec);
        } else {
            this.codec = null;
            this.copyHandler = null;
        }
    }

    /**
     * Creates a builder for an attachment type.
     *
     * <p>See {@link #builder(Class, Function)} for attachments that want to capture a reference to their holder.
     *
     * @param defaultValueSupplier A supplier for a new default value of this attachment type.
     */
    public static <T, P extends IAttachmentHolder> Builder<T, P> builder(Supplier<T> defaultValueSupplier) {
        return Builder.fromReflection(holder -> defaultValueSupplier.get());
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
    public static <T> Builder<T, IAttachmentHolder> builder(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return Builder.fromReflection(defaultValueConstructor);
    }

    public static <T, P extends IAttachmentHolder> Builder<T, P> builder(Class<P> holderClass, Function<P, T> defaultValueConstructor) {
        return Builder.fromReflection(holderClass, defaultValueConstructor);
    }

    public static class Builder<T, P extends IAttachmentHolder> {
        private final Function<P, T> defaultValueSupplier;
        private final Class<P> attachmentHolderClass;
        private final Class<T> dataType;
        @Nullable
        private Encoder<T> serializer;
        @Nullable
        private Decoder<T> deserializer;
        BiConsumer<P, T> postDeserialize;
        private boolean copyOnDeath;
        private Predicate<T> shouldSerialize;
        @Nullable
        private StreamCodec<RegistryFriendlyByteBuf, T> copyHandler;

        private Builder(Class<P> parentClass, Class<T> dataType, Function<P, T> defaultValueSupplier) {
            this.attachmentHolderClass = parentClass;
            this.dataType = dataType;
            this.defaultValueSupplier = defaultValueSupplier;
            this.shouldSerialize = Predicates.alwaysTrue();
            this.postDeserialize = (holder, inst) -> {};
        }

        private static <T, P extends IAttachmentHolder> Builder<T, P> fromReflection(Function<P, T> defaultValueSupplier) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, defaultValueSupplier.getClass());
            var attachmentHolderClass = (Class<P>) typeArgs[0];
            var dataType = (Class<T>) typeArgs[1];
            return new Builder<>(attachmentHolderClass, dataType, defaultValueSupplier);
        }

        private static <T, P extends IAttachmentHolder> Builder<T, P> fromReflection(Class<P> parentClass, Function<P, T> defaultValueSupplier) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, defaultValueSupplier.getClass());
            var dataType = (Class<T>) typeArgs[1];
            return new Builder<>(parentClass, dataType, defaultValueSupplier);
        }

        /**
         * Requests that this attachment be persisted to disk (on the logical server side), using a {@link Codec}.
         *
         * @param codec The codec to use.
         */
        public Builder<T, P> serialize(Encoder<T> codec) {
            Objects.requireNonNull(codec);
            if (this.serializer != null)
                throw new IllegalStateException("Serializer already set");

            this.serializer = codec;
            return this;
        }

        /**
         * For attachments that are serialized to disk, specifies a different codec to use for deserialization.
         *
         * @param deserializeCodec A codec to use to deserialize attachment data.
         */
        public Builder<T, P> deserialize(Decoder<T> deserializeCodec) {
            if (this.serializer == null)
                throw new IllegalStateException("Must set a serializer!");

            this.deserializer = deserializeCodec;
            return this;
        }

        /**
         * Requests that this attachment be persisted to disk (on the logical server side), using a {@link Codec}.
         *
         * @param codec           The codec to use.
         * @param shouldSerialize A check that determines whether serialization of the attachment should occur.
         */
        public Builder<T, P> serialize(Codec<T> codec, Predicate<T> shouldSerialize) {
            Objects.requireNonNull(codec);
            this.shouldSerialize = shouldSerialize;
            return serialize(codec);
        }

        /**
         * Requests that this attachment be persisted when a player respawns or when a living entity is converted.
         */
        public Builder<T, P> copyOnDeath() {
            if (this.serializer == null)
                throw new IllegalStateException("copyOnDeath requires a serializer");
            this.copyOnDeath = true;
            return this;
        }

        /**
         * Overrides the copyHandler for this attachment type.
         *
         * <p>The default copyHandler serializes the attachment and deserializes it again.
         *
         * <p>A copyHandler can only be provided for serializable attachments.
         */
        public Builder<T, P> copyHandler(StreamCodec<RegistryFriendlyByteBuf, T> cloner) {
            Objects.requireNonNull(cloner);
            // Check for serializer because only serializable attachments can be copied.
            if (this.serializer == null || this.deserializer == null)
                throw new IllegalStateException("copyHandler requires a serializer and deserializer");
            this.copyHandler = cloner;
            return this;
        }

        public Builder<T, P> copyHandler(Codec<T> codec) {
            return copyHandler(ByteBufCodecs.fromCodecWithRegistries(codec));
        }

        public AttachmentType<T> build() {
            if (this.serializer != null && this.deserializer == null) {
                if (this.serializer instanceof Codec<T> codec) {
                    this.deserializer = codec;
                } else {
                    throw new IllegalStateException("A serializer was specified but no deserializer was found for attachment: %s".formatted(this));
                }
            }

            return new AttachmentType<>(this);
        }
    }
}
