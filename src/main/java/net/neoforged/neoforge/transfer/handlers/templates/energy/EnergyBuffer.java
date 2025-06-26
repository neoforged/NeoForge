/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
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
import net.neoforged.neoforge.transfer.transaction.snapshots.GroupedSnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.snapshots.IndexedIntSnapshot;
import org.jetbrains.annotations.Nullable;

/**
 * A simple reference implementation of {@link IEnergyHandler}. Use this or implement your own if you need custom logic.
 * It is recommended to make your own implementation of {@link IEnergyHandler}.
 * <p>
 * If using this, then it is recommended to use the {@link Builder} to construct an {@link EnergyBuffer} such as:
 *
 * <pre>
 * {@code
 * EnergyBuffer.builder(1000).maxTransfer(10).build();
 * }
 * </pre>
 *
 * <p>
 * Unlike the {@link EnergyBufferComponentHandler}, the handler is mutable and is expected to be used as a block entity field, attachment, or similar.
 */
public final class EnergyBuffer implements IEnergyHandler {
    public static MapCodec<EnergyBuffer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("capacity").forGetter(data -> data.capacity),
            Codec.INT.fieldOf("max_insertion").forGetter(data -> data.maxInsert),
            Codec.INT.fieldOf("max_extraction").forGetter(data -> data.maxExtract),
            Codec.INT.fieldOf("energy").forGetter(data -> data.energy)).apply(instance, EnergyBuffer::new));

    /**
     * Example for making an attachment builder when registering
     * <p>
     *
     * <pre>{@code
     * attachment(builder(500)::build);
     * }
     * </pre>
     */
    public static AttachmentType.Builder<EnergyBuffer> attachment(Supplier<EnergyBuffer> buffer) {
        //This is done this way so that the serialized value also gets the holder using the above codec
        //Otherwise in the deserialized version, the attachment holder would always be null.
        return AttachmentType.builder(buffer)
                .serialize(holderWith(EnergyBuffer.CODEC, EnergyBuffer::setHolder));
    }

    private final IndexedIntSnapshot snapshots;

    /**
     * Current amount of energy stored in the buffer
     */
    private int energy;

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

    @Nullable
    private IAttachmentHolder holder;

    /**
     * A simple {@link IEnergyHandler} implementation.
     * <p>
     * Use of constructor is allowed, but it is HIGHLY recommended to use the builder, but using the constructor is valid.
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
     * @param capacity          Amount of energy that can be stored in any sub-buffer. Note, this value is not unique per sub-buffer.
     * @param maxInsertionRate  How much energy can be inserted in a single {@link IEnergyHandler#insert} call.
     * @param maxExtractionRate How much energy can be extracted in a single {@link IEnergyHandler#extract} call.
     * @param energy            An array of initial or serialized energy sub-buffer amounts.
     */
    public EnergyBuffer(int capacity, int maxInsertionRate, int maxExtractionRate, int energy) {
        EnergyHandlerUtil.checkEnergy(energy);
        if (capacity < 0) throw new IllegalArgumentException("Capacity must be non-negative");
        if (maxInsertionRate < 0) throw new IllegalArgumentException("MaxInsertion rate must be non-negative");
        if (maxExtractionRate < 0) throw new IllegalArgumentException("MaxExtraction rate must be non-negative");
        this.capacity = capacity;
        this.maxInsert = maxInsertionRate;
        this.maxExtract = maxExtractionRate;
        this.energy = energy;
        GroupedSnapshotJournal onChanged = GroupedSnapshotJournal.commitWith(this::onSetChanged);
        this.snapshots = IndexedIntSnapshot.of((index, amount) -> set(amount), index -> getAmount(), onChanged);
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

    @Override
    public int insert(int amount, TransactionContext transaction) {
        amount = Math.min(maxInsert, amount);
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;

        if (energy == capacity) return 0;

        int inserted = Math.min(capacity - energy, amount);
        snapshots.updateSnapshots(transaction);
        energy += inserted;
        return inserted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        amount = Math.min(maxExtract, amount);
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;
        if (EnergyHandlerUtil.checkEnergy(energy)) return 0;

        int handledAmount = Math.min(energy, amount);
        snapshots.updateSnapshots(transaction);
        energy -= handledAmount;

        return handledAmount;
    }

    @Override
    public int getAmount() {
        return this.energy;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean supportsInsertion() {
        return maxInsert > 0;
    }

    @Override
    public boolean supportsExtraction() {
        return maxExtract > 0;
    }

    private void set(int amount) {
        energy = Mth.clamp(amount, 0, capacity);
    }

    /**
     * Creates a builder of a specified capacity. This is the advised way to make an {@link EnergyBuffer}.
     * An important note, by default the transfer rate is 1% of the capacity (but never less than 1).
     * This it to help make a simple feeling of something charging.
     *
     * @param capacity How much energy the buffer is set to be able to hold.
     * @return Chainable builder to allow creation of a new {@link EnergyBuffer}
     */
    public static Builder builder(int capacity) {
        return new Builder().capacity(capacity).maxExtractRate(capacity).maxInsertRate(Mth.ceil(capacity * 0.01f));
    }

    public static class Builder {
        protected int energy = 0;
        protected int capacity;
        protected int maxInsertRate;
        protected int maxExtractRate;

        private Builder() {}

        /**
         * @param capacity How much energy the buffer can hold.
         */
        private Builder capacity(int capacity) {
            if (capacity < 0) throw new IllegalArgumentException("Capacity must be non-negative");
            this.capacity = capacity;
            return this;
        }

        /**
         * @param rate How much energy can be inserted in a single call.
         */
        public Builder maxInsertRate(int rate) {
            if (maxInsertRate < 0) throw new IllegalArgumentException("MaxInsertRate must be non-negative");
            this.maxInsertRate = rate;
            return this;
        }

        /**
         * @param rate How much energy can be extracted in a single call.
         */
        public Builder maxExtractRate(int rate) {
            if (maxExtractRate < 0) throw new IllegalArgumentException("MaxExtractRate must be non-negative");
            this.maxExtractRate = rate;
            return this;
        }

        /**
         * @param rate How much energy can be inserted or extracted in a single call.
         */
        public Builder maxTransferRate(int rate) {
            return maxExtractRate(rate).maxInsertRate(rate);
        }

        /**
         * @param amount Amount to set the initial energy buffer to
         */
        public Builder energy(int amount) {
            EnergyHandlerUtil.checkEnergy(amount);
            energy = amount;
            return this;
        }

        /**
         * Constructs a new {@link EnergyBuffer} to use from the values assigned while building.
         */
        public EnergyBuffer build() {
            return new EnergyBuffer(capacity, maxInsertRate, maxExtractRate, energy);
        }
    }

    private static <T> IAttachmentSerializer<T> holderWith(MapCodec<T> codec, BiConsumer<T, IAttachmentHolder> setter) {
        return new IAttachmentSerializer<>() {
            @Override
            public T read(IAttachmentHolder holder, ValueInput input) {
                final Optional<T> parsingResult = input.read(codec);
                T value = parsingResult.orElseThrow(this::buildException);
                setter.accept(value, holder);
                return value;
            }

            @Override
            public boolean write(T attachment, ValueOutput output) {
                //noinspection deprecation
                output.store(codec, attachment);
                return true;
            }

            private RuntimeException buildException() {
                return new IllegalStateException("Unable to read attachment due to an internal codec error.");
            }
        };
    }
}
