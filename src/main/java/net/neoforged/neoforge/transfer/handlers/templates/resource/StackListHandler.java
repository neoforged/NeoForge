/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.IStackFactory;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IIndexModifier;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.snapshots.GroupedSnapshotJournal;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

/**
 * This is provided as a simple handler to still use a stack of type {@code S} in a List as the backing data structure.
 * <p>
 * This can be used in an attachment, a block entity field, or other mutable structures.
 */
public abstract class StackListHandler<S, R extends IResource> implements IResourceHandler<R>, ValueIOSerializable {
    public static final String VALUE_IO_KEY = "stacks";

    public final int capacity;
    protected NonNullList<S> stacks;
    protected final S emptyStack;

    private int size;
    private final IStackFactory<R, S> stackFactory;
    private final List<StackJournal> snapshotJournals;
    private final GroupedSnapshotJournal onChangeJournal;
    private final Codec<NonNullList<S>> codec = NonNullList.codecOf(stackCodec());

    /**
     * @param size              How large this list will be.
     * @param capacity          How many of a single item can a single index maximally hold. This result will be the minimum value between what is set here, and the max stack size of the item.
     * @param onChangedCallback What actions should be done when the contents changed. Typically {@link BlockEntity#setChanged()} or similar.
     */
    public StackListHandler(int size, S emptyStack, int capacity, IStackFactory<R, S> stackFactory, @Nullable Runnable onChangedCallback) {
        this(NonNullList.withSize(size, emptyStack), emptyStack, capacity, stackFactory, onChangedCallback);
    }

    /**
     * @param stacks            A non-null list of stacks stored in this handler. This will make a mutable copy of the passed in list.
     * @param capacity          How many of a single item can a single index maximally hold. This result will be the minimum value between what is set here, and the max stack size of the item.
     * @param onChangedCallback What actions should be done when the contents changed. Typically {@link BlockEntity#setChanged()} or similar.
     */
    public StackListHandler(NonNullList<S> stacks, S emptyStack, int capacity, IStackFactory<R, S> stackFactory, @Nullable Runnable onChangedCallback) {
        this.capacity = capacity;

        this.stacks = mutableCopyOf(stacks);
        this.size = stacks.size();
        this.stackFactory = stackFactory;
        this.snapshotJournals = new ArrayList<>(size);
        //Creates a change journal in charge of notifying the handler has been changed.
        // The callback is usually something like someInstance::setChanged
        this.onChangeJournal = GroupedSnapshotJournal.commitWith(onChangedCallback);
        this.emptyStack = emptyStack;
        for (int i = 0; i < size; i++) {
            snapshotJournals.add(new StackJournal(i));
        }
    }

    private static <T> NonNullList<T> mutableCopyOf(NonNullList<T> list) {
        var size = list.size();
        var temp = NonNullList.<T>createWithCapacity(size);
        temp.addAll(list);
        return temp;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.store(VALUE_IO_KEY, codec, stacks);
    }

    @Override
    public void deserialize(ValueInput input) {
        Optional<NonNullList<S>> optional = input.read(codec.fieldOf(VALUE_IO_KEY));
        if (optional.isEmpty()) return;

        //Safety precaution in case the deserialized list was or ever becomes immutable.
        stacks = mutableCopyOf(optional.get());
        size = stacks.size();
    }

    @ApiStatus.OverrideOnly
    protected abstract Codec<S> stackCodec();

    @ApiStatus.OverrideOnly
    protected abstract R getResourceFrom(S stack);

    @ApiStatus.OverrideOnly
    protected abstract int getAmountFrom(S stack);

    @ApiStatus.OverrideOnly
    protected int getCapacityFrom(R stack) {
        return Integer.MAX_VALUE;
    }

    @ApiStatus.OverrideOnly
    protected abstract boolean matches(R resource, S stack);

    @ApiStatus.OverrideOnly
    protected abstract S snapshotOf(S stack);

    /**
     * Copies all the contents of this handler to a non-null list of the same size.
     *
     * @return A new non-null list.
     */
    @Contract(pure = true)
    public NonNullList<S> copyToList() {
        NonNullList<S> list = NonNullList.withSize(size(), emptyStack);
        int size = size();
        for (int index = 0; index < size; index++) {
            list.set(index, stacks.get(index));
        }
        return list;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public R getResource(int index) {
        Objects.checkIndex(index, size());
        return getResourceFrom(stacks.get(index));
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return getAmountFrom(stacks.get(index));
    }

    @Override
    public int getCapacity(int index, R resource) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty()) return capacity;
        return Math.min(capacity, getCapacityFrom(resource));
    }

    @Override
    public boolean isValid(int index, R resource) {
        Objects.checkIndex(index, size());
        return true;
    }

    @Override
    public boolean supportsInsertion(int index) {
        Objects.checkIndex(index, size());
        return true;
    }

    @Override
    public boolean supportsExtraction(int index) {
        Objects.checkIndex(index, size());
        return true;
    }

    @Override
    public int insert(R resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handled = 0;
        for (int index = 0; index < size; index++) {
            handled += insertBehaviour(index, resource, amount - handled, context);
            if (handled == amount)
                break;
        }
        return handled;
    }

    @Override
    public int insert(int index, R resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        return insertBehaviour(index, resource, amount, context);
    }

    private int insertBehaviour(int index, R resource, int amount, TransactionContext transaction) {
        if (!isValid(index, resource)) return 0;

        S currentStack = stacks.get(index);
        int currentAmount = getAmountFrom(currentStack);

        int capacity = getCapacity(index, resource);
        if (currentAmount >= capacity || (currentAmount > 0 && !matches(resource, currentStack))) return 0;
        int inserted = Math.min(amount, capacity - currentAmount);
        if (inserted > 0) {
            snapshotJournals.get(index).updateSnapshots(transaction);
            setInternal(index, resource, inserted + currentAmount);
        }
        return inserted;
    }

    @Override
    public int extract(int index, R resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        return extractBehaviour(index, resource, amount, context);
    }

    @Override
    public int extract(R resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        int handled = 0;
        for (int index = 0; index < size; index++) {
            handled += extractBehaviour(index, resource, amount - handled, context);
            if (handled == amount) break;
        }
        return handled;
    }

    private int extractBehaviour(int index, R resource, int amount, TransactionContext transaction) {
        S currentStack = stacks.get(index);

        if (!matches(resource, currentStack)) return 0;
        int currentAmount = getAmountFrom(currentStack);
        int handledAmount = Math.min(amount, currentAmount);
        if (handledAmount > 0) {
            snapshotJournals.get(index).updateSnapshots(transaction);
            setInternal(index, resource, currentAmount - handledAmount);
        }
        return handledAmount;
    }

    /**
     * Exposed set to be used for {@link ResourceHandlerSlot} as an {@link IIndexModifier}
     *
     * @param index    index that the resource is at
     * @param resource resource intended to overwrite the current value
     * @param amount   the amount of the resource desired to be at the specified index
     */
    public void set(int index, R resource, int amount) {
        setInternal(index, resource, amount);
        onChangeJournal.runCommitCallback();
    }

    @ApiStatus.OverrideOnly
    protected void setInternal(int index, R resource, int amount) {
        stacks.set(index, stackFactory.create(resource, amount));
    }

    private class StackJournal extends SnapshotJournal<S> {
        private final int index;

        private StackJournal(int index) {
            this.index = index;
        }

        @Override
        public void updateSnapshots(TransactionContext transaction) {
            onChangeJournal.updateSnapshots(transaction);
            super.updateSnapshots(transaction);
        }

        @Override
        protected S createSnapshot() {
            S original = stacks.get(index);
            return snapshotOf(original);
        }

        @Override
        protected void revertToSnapshot(S snapshot) {
            stacks.set(index, snapshot);
        }

        @Override
        protected void onCommit(S originalState) {
            onChangeJournal.runCommitCallback();
        }
    }
}
