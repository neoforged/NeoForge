/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import org.jetbrains.annotations.Nullable;

/**
 * A data storage for mutable resource stacks. This data can be put anywhere (with limited exceptions such as DataComponents), but it was designed with {@link net.neoforged.neoforge.attachment.AttachmentType DataAttachments} in mind.
 * You are able to build new containers, slice existing ones, as well as convert them to other types such as an {@link net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler IEnergyHandler}.
 * To be more clear, the container itself, but by calling {@link #asHandler()} it will create one, though it is recommended you cache this rather than call it every time you need a handler of the container.
 * <p>
 *
 * <strong>Example Usage</strong>
 *
 * <pre>
 * {@code
 * var container = SimpleItemResourceContainer.from(someSerializedList)
 *         .onChange(this::markHolderAsDirty)
 *         .build();
 * IResourceHandler<ItemResource> handler = container.asHandler();
 * var outputContainer = container.slice(3, 4);
 * var outputHandler = outputContainer.asHandler(IHandleIOBehaviour.EXTRACT_ONLY);
 * }
 * </pre>
 *
 * <p>
 * To reiterate, this can work anywhere in a mutable context, but things like {@link net.minecraft.core.component.DataComponentType DataComponents} that require an immutable scope will not work properly.
 */
//Originally written by Soaryn for XyCraft adopted from Amadornes's ItemContainer.
public class EnergyContainer implements IEnergyContainer {
    private final int[] energyValues;
    private final int size;
    private final int capacity;
    private final int maxExtractRate;
    private final int maxInsertRate;
    @Nullable
    private final Runnable updateCallback;
    private final ArrayList<IndexedIntSnapshot> snapshots = new ArrayList<>();

    protected EnergyContainer(int[] energyValues, int capacity, int maxInsertRate, int maxExtractRate, @Nullable Runnable updateCallback) {
        Objects.requireNonNull(energyValues);
        Objects.checkIndex(0, energyValues.length);

        this.size = energyValues.length;
        this.energyValues = energyValues;
        this.updateCallback = updateCallback;
        this.capacity = capacity;
        this.maxInsertRate = maxInsertRate;
        this.maxExtractRate = maxExtractRate;

        snapshots.ensureCapacity(size);
        for (var i = 0; i < size; i++) {
            snapshots.add(new IndexedIntSnapshot(i));
        }
    }

    @Override
    public SnapshotJournal<?> getSnapshotJournal(int index) {
        return snapshots.get(index);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int get(int index) {
        Objects.checkIndex(index, size());
        return energyValues[index];
    }

    @Override
    public int getCapacity(int index) {
        return capacity;
    }

    @Override
    public int getMaxInsertRate() {
        return maxInsertRate;
    }

    @Override
    public int getMaxExtractRate() {
        return maxExtractRate;
    }

    @Override
    public void set(int index, int value) {
        Objects.checkIndex(index, size());
        energyValues[index] = value;
        if (updateCallback != null)
            updateCallback.run();
    }

    @Override
    public void clear() {
        Arrays.fill(energyValues, 0);
        if (updateCallback != null)
            updateCallback.run();
    }

    @Override
    public IEnergyContainer slice(int from, int to) {
        Objects.checkFromToIndex(from, to, size());
        return new Slice(from, to - from);
    }

    private class Slice implements IEnergyContainer {
        private final int start, length;

        public Slice(int start, int length) {
            this.start = start;
            this.length = length;
        }

        @Override
        public int size() {
            return length;
        }

        @Override
        public int getMaxInsertRate() {
            return EnergyContainer.this.getMaxInsertRate();
        }

        @Override
        public int getMaxExtractRate() {
            return EnergyContainer.this.getMaxExtractRate();
        }

        @Override
        public int get(int index) {
            Objects.checkIndex(index, size()); //audit called in the this.get
            return EnergyContainer.this.get(index + start);
        }

        @Override
        public void set(int index, int stack) {
            Objects.checkIndex(index, size()); //audit called in the this.get
            EnergyContainer.this.set(index + start, stack);
        }

        @Override
        public int getCapacity(int index) {
            Objects.checkIndex(index, size()); //audit called in the this.get
            return EnergyContainer.this.getCapacity(index + start);
        }

        @Override
        public SnapshotJournal<?> getSnapshotJournal(int index) {
            return EnergyContainer.this.getSnapshotJournal(index + start);
        }

        @Override
        public void clear() {
            //We don't want to clear everything, just the subset
            for (int i = 0; i < length; i++)
                EnergyContainer.this.energyValues[i + start] = 0;
            if (EnergyContainer.this.updateCallback != null)
                EnergyContainer.this.updateCallback.run();
        }

        @Override
        public IEnergyContainer slice(int from, int to) {
            Objects.checkFromToIndex(from, to, length);
            return new Slice(this.start + from, to - from);
        }
    }

    public static class Builder<TBuilder extends Builder<TBuilder>> {
        protected int[] energyValues = new int[0];
        protected int capacity;
        protected int maxInsertRate;
        protected int maxExtractRate;
        @Nullable
        protected Runnable updateCallback;

        public Builder() {}

        private TBuilder self() {
            //noinspection unchecked
            return (TBuilder) this;
        }

        public TBuilder size(int size) {
            energyValues = new int[size];
            Arrays.fill(energyValues, 0);
            return self();
        }

        public TBuilder capacity(int capacity) {
            this.capacity = capacity;
            return self();
        }

        public TBuilder maxInsertRate(int maxInsertRate) {
            this.maxInsertRate = maxInsertRate;
            return self();
        }

        public TBuilder maxExtractRate(int maxExtractRate) {
            this.maxExtractRate = maxExtractRate;
            return self();
        }

        public TBuilder onChange(Runnable updateCallback) {
            this.updateCallback = updateCallback;
            return self();
        }

        public TBuilder from(int[] values) {
            this.energyValues = Arrays.copyOf(values, values.length);
            return self();
        }

        public EnergyContainer build() {
            return new EnergyContainer(energyValues, capacity, maxInsertRate, maxExtractRate, updateCallback);
        }
    }

    private class IndexedIntSnapshot extends SnapshotJournal<Integer> {
        private final int index;

        public IndexedIntSnapshot(int index) {
            this.index = index;
        }

        @Override
        protected Integer createSnapshot() {
            return get(index);
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            set(index, snapshot);
        }
    }
}
