/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.transfer.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
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
 *
 * <p>
 * Unlike the {@link EnergyBufferComponentHandler}, the handler also is the attachment data.
 */
public final class EnergyBufferAttachment implements IEnergyHandler {
    public static MapCodec<EnergyBufferAttachment> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
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

    private final ArrayList<IndexedIntSnapshot> snapshots;

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
        if (size < energy.length) {
            throw new IllegalArgumentException("An EnergyBuffer must have a size (" + size + ") larger than energy array length (" + energy.length + ") passed in.");
        }
        if (capacity < 0) throw new IllegalArgumentException("Capacity must be non-negative");
        if (maxInsertionRate < 0) throw new IllegalArgumentException("MaxInsertion rate must be non-negative");
        if (maxExtractionRate < 0) throw new IllegalArgumentException("MaxExtraction rate must be non-negative");
        this.size = size;
        this.capacity = capacity;
        this.maxInsert = maxInsertionRate;
        this.maxExtract = maxExtractionRate;
        this.energy = new int[size];
        for (int i = 0; i < size; i++) {
            if (energy[i] < 0) throw new IllegalArgumentException("Energy at index " + i + " must be non-negative");
            this.energy[i] = Math.max(0, Math.min(capacity, energy[i]));
        }
        this.snapshots = IndexedIntSnapshot.listOf(this::set, this::getAmount, SetChangedSnapshot.of(this::onSetChanged), size);
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
    public int insert(int amount, TransactionContext transaction) {
        amount = Math.min(maxInsert, amount);
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;

        int handled = 0;
        int indices = size();
        for (int index = 0; index < indices; index++) {
            //We don't need to check if the index is valid in this case since we already know our index is within bounds
            handled += insertCommon(index, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    @Override
    public int insert(int index, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        amount = Math.min(maxInsert, amount);
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;

        return insertCommon(index, amount, transaction);
    }

    /**
     * This was chosen to be separate from {@link EnergyBufferAttachment#insert(int, int, TransactionContext)} to provide both parity
     * with the IResourceHandler and allow more accurate index checks when doing the loop variant.
     * <p>
     * The added benefit is less double-checking in runtime on data we already know
     */
    private int insertCommon(int index, int amount, TransactionContext transaction) {
        if (energy[index] == capacity) return 0;

        int inserted = Math.min(capacity - energy[index], amount);
        snapshots.get(index).updateSnapshots(transaction);
        energy[index] += inserted;
        return inserted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        amount = Math.min(maxExtract, amount);
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;

        int handled = 0;
        int indices = size();
        for (int index = 0; index < indices; index++) {
            //We don't need to check if the index is valid in this case since we already know our index is within bounds
            handled += extractCommon(index, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    @Override
    public int extract(int index, int amount, TransactionContext transaction) {
        //This check is done per external index call
        Objects.checkIndex(index, size());
        amount = Math.min(maxExtract, amount);
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;

        return extractCommon(index, amount, transaction);
    }

    /**
     * Common method for extraction, but allowing the index-less and the indexed methods to have their
     * own validations for their respective calls. Avoids double-checking certain validations
     */
    private int extractCommon(int index, int amount, TransactionContext transaction) {
        if (energy[index] == 0) return 0;

        int handledAmount = Math.min(energy[index], amount);
        snapshots.get(index).updateSnapshots(transaction);
        energy[index] -= handledAmount;
        return handledAmount;
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return this.energy[index];
    }

    @Override
    public int getCapacity(int index) {
        Objects.checkIndex(index, size());
        return capacity;
    }

    @Override
    public boolean supportsInsertion(int index) {
        Objects.checkIndex(index, size());
        return maxInsert > 0;
    }

    @Override
    public boolean supportsExtraction(int index) {
        Objects.checkIndex(index, size());
        return maxExtract > 0;
    }

    private void set(int index, int amount) {
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
    public static Builder builder(int size, int capacity) {
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
        public Builder capacity(int capacity) {
            if (capacity < 0) throw new IllegalArgumentException("Capacity must be non-negative");
            this.capacity = capacity;
            return this;
        }

        /**
         * @param rate How much energy the buffer can insert in a single call.
         */
        public Builder maxInsertRate(int rate) {
            if (maxInsertRate < 0) throw new IllegalArgumentException("MaxInsertRate must be non-negative");
            this.maxInsertRate = rate;
            return this;
        }

        /**
         * @param rate How much energy the buffer can extract in a single call.
         */
        public Builder maxExtractRate(int rate) {
            if (maxExtractRate < 0) throw new IllegalArgumentException("MaxExtractRate must be non-negative");
            this.maxExtractRate = rate;
            return this;
        }

        /**
         * @param rate How much energy the buffer can insert or extract in a single call.
         */
        public Builder maxTransferRate(int rate) {
            return maxExtractRate(rate).maxInsertRate(rate);
        }

        /**
         * @param amount Amount to set the initial energy buffer to at the specified index
         */
        public Builder energy(int index, int amount) {
            Objects.checkIndex(index, size);
            if (amount < 0) throw new IllegalArgumentException("Energy at index " + index + " must be non-negative");
            this.energy[index] = amount;
            return this;
        }

        /**
         * @param amount Amount to set the initial energy buffer to all indices
         */
        public Builder energy(int amount) {
            for (int index = 0; index < size; index++) {
                this.energy[index] = amount;
            }
            return this;
        }

        /**
         * @param size Number of sub-buffers. In this structure, the size needs to be at least 1 due to the bound checks in the {@link EnergyBufferAttachment}
         */
        public Builder size(int size) {
            if (size < 0) throw new IllegalArgumentException("Size must be non-negative");
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

    public static <T> IAttachmentSerializer<T> holderWith(MapCodec<T> codec, BiConsumer<T, IAttachmentHolder> setter) {
        return new IAttachmentSerializer<>() {
            @Override
            public T read(IAttachmentHolder holder, ValueInput input) {
                final Optional<T> parsingResult = input.read(codec);
                var value = parsingResult.orElseThrow(() -> buildException("read"));
                setter.accept(value, holder);
                return value;
            }

            @Override
            public boolean write(T attachment, ValueOutput output) {
                output.store(codec, attachment);
                return true;
            }

            private RuntimeException buildException(final String operation) {
                return new IllegalStateException("Unable to " + operation + " attachment due to an internal codec error.");
            }
        };
    }
}
