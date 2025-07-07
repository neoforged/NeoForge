/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
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
 * <li>Modifications to attachments should be followed by a call to {@link ChunkAccess#markUnsaved}.</li>
 * <li>Serializable attachments are copied from a {@link ProtoChunk} to a {@link LevelChunk} on promotion.</li>
 * </ul>
 */
public final class AttachmentType<T> {
    final Function<IAttachmentHolder, T> defaultValueSupplier;
    @Nullable
    final IAttachmentSerializer<T> serializer;
    final boolean copyOnDeath;
    final IAttachmentCopyHandler<T> copyHandler;
    @Nullable
    AttachmentSyncHandler<T> syncHandler;

    private AttachmentType(Builder<T> builder) {
        this.defaultValueSupplier = builder.defaultValueSupplier;
        this.serializer = builder.serializer;
        this.copyOnDeath = builder.copyOnDeath;
        this.copyHandler = builder.copyHandler != null ? builder.copyHandler : defaultCopyHandler(serializer);
        this.syncHandler = builder.syncHandler;
    }

    private static <T> IAttachmentCopyHandler<T> defaultCopyHandler(@Nullable IAttachmentSerializer<T> serializer) {
        if (serializer == null) {
            return (attachment, holder, provider) -> {
                throw new UnsupportedOperationException("Cannot copy non-serializable attachments");
            };
        }
        return (attachment, holder, provider) -> {
            ProblemReporter.Collector reporter = new ProblemReporter.Collector();
            var output = TagValueOutput.createWithContext(reporter, provider);
            if (!serializer.write(attachment, output)) {
                return null;
            }
            if (!reporter.isEmpty()) {
                throw new IllegalArgumentException("Attachment failed to serialise during copy: " + reporter.getReport());
            }

            reporter = new ProblemReporter.Collector();
            var input = TagValueInput.create(reporter, provider, output.buildResult());
            var attach = serializer.read(holder, input);
            if (!reporter.isEmpty()) {
                throw new IllegalArgumentException("Attachment failed to deserialise during copy: " + reporter.getReport());
            }
            return attach;
        };
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
     * Create a builder for an attachment type that uses {@link ValueIOSerializable} for serialization.
     * Other kinds of serialization can be implemented using {@link #builder(Supplier)} and {@link Builder#serialize(IAttachmentSerializer)}.
     *
     * <p>See {@link #serializable(Function)} for attachments that want to capture a reference to their holder.
     */
    public static <T extends ValueIOSerializable> Builder<T> serializable(Supplier<T> defaultValueSupplier) {
        return serializable(holder -> defaultValueSupplier.get());
    }

    /**
     * Create a builder for an attachment type that uses {@link ValueIOSerializable} for serialization.
     * Other kinds of serialization can be implemented using {@link #builder(Supplier)} and {@link Builder#serialize(IAttachmentSerializer)}.
     *
     * <p>This overload allows capturing a reference to the {@link IAttachmentHolder} for the attachment.
     * To obtain a specific subtype, the holder can be cast.
     * If the holder is of the wrong type, the constructor should throw an exception.
     * See {@link #serializable(Supplier)} for an overload that does not capture the holder.
     */
    public static <T extends ValueIOSerializable> Builder<T> serializable(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return builder(defaultValueConstructor).serialize(new IAttachmentSerializer<>() {
            @Override
            public T read(IAttachmentHolder holder, ValueInput input) {
                var ret = defaultValueConstructor.apply(holder);
                ret.deserialize(input);
                return ret;
            }

            @Override
            public boolean write(T attachment, ValueOutput output) {
                attachment.serialize(output);
                return true;
            }
        });
    }

    public static class Builder<T> {
        private final Function<IAttachmentHolder, T> defaultValueSupplier;
        @Nullable
        private IAttachmentSerializer<T> serializer;
        private boolean copyOnDeath;
        @Nullable
        private IAttachmentCopyHandler<T> copyHandler;
        @Nullable
        private AttachmentSyncHandler<T> syncHandler;

        private Builder(Function<IAttachmentHolder, T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
        }

        /**
         * Requests that this attachment be persisted to disk (on the logical server side).
         *
         * @param serializer The serializer to use.
         */
        public Builder<T> serialize(IAttachmentSerializer<T> serializer) {
            Objects.requireNonNull(serializer);
            if (this.serializer != null)
                throw new IllegalStateException("Serializer already set");

            this.serializer = serializer;
            return this;
        }

        /**
         * Requests that this attachment be persisted to disk (on the logical server side), using a {@link Codec}.
         *
         * <p>Codec-based attachments cannot capture a reference to their holder.
         *
         * @param codec The codec to use.
         */
        public Builder<T> serialize(MapCodec<T> codec) {
            return serialize(codec, Predicates.alwaysTrue());
        }

        /**
         * Requests that this attachment be persisted to disk (on the logical server side), using a {@link Codec}.
         *
         * <p>Codec-based attachments cannot capture a reference to their holder.
         *
         * @param codec           The codec to use.
         * @param shouldSerialize A check that determines whether serialization of the attachment should occur.
         */
        public Builder<T> serialize(MapCodec<T> codec, Predicate<? super T> shouldSerialize) {
            Objects.requireNonNull(codec);
            return serialize(new IAttachmentSerializer<>() {
                @Override
                public T read(IAttachmentHolder holder, ValueInput input) {
                    final Optional<T> parsingResult = input.read(codec);
                    return parsingResult.orElseThrow(() -> buildException("read"));
                }

                @Override
                public boolean write(T attachment, ValueOutput output) {
                    if (!shouldSerialize.test(attachment)) {
                        return false;
                    }
                    output.store(codec, attachment);
                    return true;
                }

                private RuntimeException buildException(final String operation) {
                    return new IllegalStateException("Unable to " + operation + " attachment due to an internal codec error.");
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
         * Overrides the copyHandler for this attachment type.
         *
         * <p>The default copyHandler serializes the attachment and deserializes it again.
         *
         * <p>A copyHandler can only be provided for serializable attachments.
         */
        public Builder<T> copyHandler(IAttachmentCopyHandler<T> cloner) {
            Objects.requireNonNull(cloner);
            // Check for serializer because only serializable attachments can be copied.
            if (this.serializer == null)
                throw new IllegalStateException("copyHandler requires a serializer");
            this.copyHandler = cloner;
            return this;
        }

        /**
         * Requests that this attachment be synced to clients using the provided {@code syncHandler}.
         */
        public Builder<T> sync(AttachmentSyncHandler<T> syncHandler) {
            Objects.requireNonNull(syncHandler);
            this.syncHandler = syncHandler;
            return this;
        }

        /**
         * Requests that this attachment be synced to all clients that receive the holding object.
         *
         * <p>The full data is always written using the provided stream codec.
         */
        public Builder<T> sync(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            return sync((holder, to) -> true, streamCodec);
        }

        /**
         * Requests that this attachment be synced to some clients.
         *
         * <p>The full data is always written using the provided stream codec.
         *
         * @param sendToPlayer A predicate that determines whether the data should be sent to a specific player that receives the holding object.
         * @see AttachmentSyncHandler#sendToPlayer
         */
        public Builder<T> sync(BiPredicate<IAttachmentHolder, ServerPlayer> sendToPlayer, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            Objects.requireNonNull(sendToPlayer);
            Objects.requireNonNull(streamCodec);
            return sync(new AttachmentSyncHandler<>() {
                @Override
                public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
                    return sendToPlayer.test(holder, to);
                }

                @Override
                public void write(RegistryFriendlyByteBuf buf, T attachment, boolean initialSync) {
                    streamCodec.encode(buf, attachment);
                }

                @Override
                public T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable T previousValue) {
                    return streamCodec.decode(buf);
                }
            });
        }

        public AttachmentType<T> build() {
            return new AttachmentType<>(this);
        }
    }
}
