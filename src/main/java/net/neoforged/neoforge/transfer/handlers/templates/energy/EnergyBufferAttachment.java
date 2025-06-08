/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nonnegative;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandlerModifiable;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.snapshots.SetChangedSnapshot;
import org.jetbrains.annotations.Nullable;

// PRIMER: NBT Serialization was removed in favor of either Codecs in data attachments, or alternate means of writing to nbt.

/**
 * A simple reference implementation of {@link IEnergyHandler}. Use this or implement your own if you need custom logic. This has multiple "slots" or sub-buffers for energy to be inserted or extracted from.
 * It is recommended to make your own implementation of {@link IEnergyHandler}, especially if you need multiple "sub buffers".
 * <p>
 * It is also recommended to use the {@link Builder} to construct an {@link EnergyBufferAttachment} such as:
 *
 * <pre>
 * {@code
 * EnergyBuffer.builder(3, 1000).maxTransfer(10).build();
 * }
 * </pre>
 */
public final class EnergyBufferAttachment implements IEnergyHandlerModifiable {
    public static Codec<EnergyBufferAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("size").forGetter(data -> data.size),
            Codec.INT.fieldOf("capacity").forGetter(data -> data.capacity),
            Codec.INT.fieldOf("max_insertion").forGetter(data -> data.maxInsert),
            Codec.INT.fieldOf("max_extraction").forGetter(data -> data.maxExtract),
            Codec.INT_STREAM.fieldOf("energy").xmap(IntStream::toArray, IntStream::of).forGetter(data -> data.energy)).apply(instance, EnergyBufferAttachment::new));

    //untested yet.
    /**
     * Example for making an attachment builder when registering
     * <p>
     *
     * <pre>{@code
     * attachment(builder(10, 512)::build);
     * }
     * </pre>
     */
    public static AttachmentType.Builder<EnergyBufferAttachment> attachment(Supplier<EnergyBufferAttachment> buffer) {
        return AttachmentType.builder(buffer).serialize(holderWith(EnergyBufferAttachment.CODEC, EnergyBufferAttachment::setHolder));
    }

    private final ArrayList<IndexedIntSnapshot> snapshots = new ArrayList<>();

    /**
     * Number of sub-buffers
     */
    private final int size;

    /**
     * Current amount of energy stored in the buffer
     */
    private final int[] energy;

    /**
     * How much energy can be stored in each sub buffer. This capacity is, while unique to each buffer, the value is the same.
     */
    private final int capacity;

    /**
     * How much energy can be inserted in a single call of `insert`.
     * Note, if you need to limit how much can be inserted in a single tick,
     * then you will need to make your own implementation of {@link IEnergyHandler} that has the required information.
     */
    private final int maxInsert;

    /**
     * How much energy can be inserted in a single call of `extract`.
     * Note, if you need to limit how much can be extracted in a single tick,
     * then you will need to make your own implementation of {@link IEnergyHandler} that has the required information.
     */
    private final int maxExtract;

    //Some holder (usually a blockentity) that will be set on being serialized
    @Nullable
    private IAttachmentHolder holder;

    /**
     * An {@link IEnergyHandler} with a variable, but constant, amount of sub-buffers.
     * This will give more options to what is routing the energy to the handler. If you don't need more than one sub-buffer, the builder can be used with a size of `1`, but it is more advisable, create your own {@link IEnergyHandler} implementation to match your exact needs.
     * <p>
     * Use of constructor is allowed, but it is HIGHLY recommended to use the builder. Only use this if you are confident you need something specific.
     * <p>
     * Example:
     *
     * <pre>
     * {@code
     * //Creates a buffer that has 3 sub-buffers each with a capacity of 1000,
     * // and a max insert and extraction rate of 10
     * EnergyBuffer.builder(3, 1000).maxTransfer(10).build();
     * }
     * </pre>
     *
     * @param size              How many sub-buffers should be available.
     * @param capacity          Amount of energy that can be stored in any sub-buffer. Note, this value is not unique per sub-buffer.
     * @param maxInsertionRate  How much energy can be inserted in a single {@link IEnergyHandler#insert} call.
     * @param maxExtractionRate How much energy can be extracted in a single {@link IEnergyHandler#extract} call.
     * @param energy            An array of initial or serialized energy sub-buffer amounts.
     */
    public EnergyBufferAttachment(int size, int capacity, int maxInsertionRate, int maxExtractionRate, int... energy) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must not be less than zero");
        }
        if (size < energy.length) {
            throw new IllegalArgumentException("An EnergyBuffer must have a size (" + size + ") larger than energy array length (" + energy.length + ") passed in.");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("The capacity in an EnergyBuffer must not be less than zero.");
        }
        if (maxInsertionRate < 0) {
            throw new IllegalArgumentException("The maximum insertion rate in an EnergyBuffer must not be less than zero.");
        }
        if (maxExtractionRate < 0) {
            throw new IllegalArgumentException("The maximum extraction rate in an EnergyBuffer must not be less than zero.");
        }
        this.size = size;
        this.capacity = capacity;
        this.maxInsert = maxInsertionRate;
        this.maxExtract = maxExtractionRate;
        this.energy = new int[size];
        for (int i = 0; i < size; i++) {
            this.energy[i] = Math.max(0, Math.min(capacity, energy[i]));
        }
        IndexedIntSnapshot.initSnapshots(this, snapshots, size, SetChangedSnapshot.of(this::onSetChanged));
    }

    //Attachment building methods
    private void setHolder(IAttachmentHolder holder) {
        this.holder = holder;
    }

    private void onSetChanged() {
        if (holder instanceof BlockEntity entity) {
            entity.setChanged();
        }
    }

    //IEnergyHandler

    @Override
    public int size() {
        return size;
    }

    @Override
    @Nonnegative
    public int insert(@Nonnegative int amount, TransactionContext transaction) {
        amount = Math.min(maxInsert, amount);
        if (amount <= 0) return 0;

        var handled = 0;
        var indices = size();
        for (var index = 0; index < indices; index++) {
            //We don't need to check if the index is valid in this case since we already know our index is within bounds
            handled += insertCommon(index, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    @Override
    @Nonnegative
    public int insert(int index, @Nonnegative int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        amount = Math.min(maxInsert, amount);
        if (amount <= 0) return 0;

        return insertCommon(index, amount, transaction);
    }

    /**
     * This was chosen to be separate from {@link EnergyBufferAttachment#insert(int, int, TransactionContext)} to provide both parity
     * with the IResourceHandler and allow more accurate index checks when doing the loop variant.
     * <p>
     * The added benefit is less double-checking in runtime on data we already know
     */
    @Nonnegative
    private int insertCommon(@Nonnegative int index, @Nonnegative int amount, TransactionContext transaction) {
        if (energy[index] == capacity) return 0;

        int inserted = Math.min(capacity - energy[index], amount);
        snapshots.get(index).updateSnapshots(transaction);
        energy[index] += inserted;
        return inserted;
    }

    @Override
    @Nonnegative
    public int extract(@Nonnegative int amount, TransactionContext transaction) {
        amount = Math.min(maxExtract, amount);
        if (amount <= 0) return 0;

        var handled = 0;
        var indices = size();
        for (var index = 0; index < indices; index++) {
            //We don't need to check if the index is valid in this case since we already know our index is within bounds
            handled += extractCommon(index, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    @Override
    @Nonnegative
    public int extract(@Nonnegative int index, @Nonnegative int amount, TransactionContext transaction) {
        //This check is done per external index call
        Objects.checkIndex(index, size());
        amount = Math.min(maxExtract, amount);
        if (amount <= 0) return 0;

        return extractCommon(index, amount, transaction);
    }

    /**
     * Common method for extraction, but allowing the index-less and the indexed methods to have their
     * own validations for their respective calls. Avoids double-checking certain validations
     */
    private int extractCommon(@Nonnegative int index, @Nonnegative int amount, TransactionContext transaction) {
        if (energy[index] == 0) return 0;

        int handledAmount = Math.min(energy[index], amount);
        snapshots.get(index).updateSnapshots(transaction);
        energy[index] -= handledAmount;
        return handledAmount;
    }

    @Override
    @Nonnegative
    public int getAmount(@Nonnegative int index) {
        Objects.checkIndex(index, size());
        return this.energy[index];
    }

    @Override
    @Nonnegative
    public int getCapacity(@Nonnegative int index) {
        return capacity;
    }

    @Override
    public boolean supportsInsertion(@Nonnegative int index) {
        return maxInsert > 0;
    }

    @Override
    public boolean supportsExtraction(@Nonnegative int index) {
        return maxExtract > 0;
    }

    @Override
    public void set(@Nonnegative int index, @Nonnegative int amount) {
        //blind trust that the index is in bounds when using modifiable
        energy[index] = Mth.clamp(amount, 0, capacity);
    }

    /**
     * Creates a builder of a specified size, and capacity. This is the advised way to make an {@link EnergyBufferAttachment}.
     * An important note, is by default the transfer rate is 1% of the capacity (but never less than 1).
     * This it to help make a simple feeling of something charging
     *
     * @param size     How many sub-buffers the {@link EnergyBufferAttachment} should have. The typical amount is 1, but more advanced usages can have more.
     * @param capacity How much energy the sub-buffers are set to be able to hold individually. If you desire separate capacities per buffer, then you will need to implement your own variant.
     * @return Chainable builder to allow creation of a new {@link EnergyBufferAttachment}
     */
    public static Builder builder(@Nonnegative int size, @Nonnegative int capacity) {
        return new Builder().size(size).capacity(capacity).maxTransferRate(Mth.ceil(capacity * 0.01f));
    }

    public static class Builder {
        protected int size = 1;
        protected int[] energy = new int[1];
        protected int capacity;
        protected int maxInsertRate;
        protected int maxExtractRate;

        private Builder() {}

        /**
         * @param capacity How much energy each sub-buffer can hold.
         */
        public Builder capacity(@Nonnegative int capacity) {
            this.capacity = capacity;
            return this;
        }

        /**
         * @param rate How much energy the buffer can insert in a single call.
         */
        public Builder maxInsertRate(@Nonnegative int rate) {
            this.maxInsertRate = rate;
            return this;
        }

        /**
         * @param rate How much energy the buffer can extract in a single call.
         */
        public Builder maxExtractRate(@Nonnegative int rate) {
            this.maxExtractRate = rate;
            return this;
        }

        /**
         * @param rate How much energy the buffer can insert or extract in a single call.
         */
        public Builder maxTransferRate(@Nonnegative int rate) {
            return maxExtractRate(rate).maxInsertRate(rate);
        }

        /**
         * @param amount Amount to set the initial energy buffer to at the specified index
         */
        public Builder energy(@Nonnegative int index, @Nonnegative int amount) {
            Objects.checkIndex(index, size);
            this.energy[index] = amount;
            return this;
        }

        /**
         * @param amount Amount to set the initial energy buffer to all indices
         */
        public Builder energy(@Nonnegative int amount) {
            for (int index = 0; index < size; index++) {
                this.energy[index] = amount;
            }
            return this;
        }

        /**
         * @param size Number of sub-buffers. In this structure, the size needs to be at least 1 due to the bound checks in the {@link EnergyBufferAttachment}
         */
        //TODO this was set to 1-> max int, however, it should be fine to be zero.
        public Builder size(@Nonnegative int size) {
            this.size = size;
            energy = new int[size];
            return this;
        }

        /**
         * Constructs a new {@link EnergyBufferAttachment} to use.
         */
        public EnergyBufferAttachment build() {
            return new EnergyBufferAttachment(size, capacity, maxInsertRate, maxExtractRate, energy);
        }
    }

    public static <T> IAttachmentSerializer<Tag, T> holderWith(Codec<T> codec, BiConsumer<T, IAttachmentHolder> setter) {
        return new IAttachmentSerializer<>() {
            @Override
            public T read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider provider) {
                var parse = codec.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag);
                if (parse.error().isPresent()) {
                    throw new RuntimeException(parse.error().get().toString());
                }
                if (parse.result().isEmpty())
                    throw new RuntimeException("Result not present");

                var data = parse.result().get();
                setter.accept(data, holder);
                return data;
            }

            @Override
            public Tag write(T attachment, HolderLookup.Provider provider) {
                var encode = codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), attachment);
                if (encode.error().isPresent()) {
                    throw new RuntimeException(encode.error().get().toString());
                }
                if (encode.result().isEmpty())
                    throw new RuntimeException("Result not present");

                return encode.result().get();
            }
        };
    }
}
