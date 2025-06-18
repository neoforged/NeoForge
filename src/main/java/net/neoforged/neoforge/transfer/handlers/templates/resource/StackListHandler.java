/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.snapshots.SetChangedSnapshot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * This is provided as a simple handler to still use a {@link ItemStack} in a List as the backing data structure.
 * It is advised to use a {@link ItemResource} or similar form of {@link IResourceStack}.
 * <p>
 * This can be used in an attachment, a block entity field, or other mutable structures.
 */
public abstract class StackListHandler<S, R extends IResource> implements IResourceHandler<R> {
    public final int size;
    public final int capacity;

    private final NonNullList<S> stacks;
    private final ArrayList<StackJournal> snapshotJournals = new ArrayList<>();
    private final SetChangedSnapshot onChangeJournal;
    private final S emptyStack;

    /**
     * @param size              How large this list will be.
     * @param capacity          How many of a single item can a single index maximally hold. This result will be the minimum value between what is set here, and the max stack size of the item.
     * @param onChangedCallback What actions should be done when the contents changed. Typically {@link BlockEntity#setChanged()} or similar.
     */
    public StackListHandler(int size, S emptyStack, int capacity, @Nullable Runnable onChangedCallback) {
        this(NonNullList.withSize(size, emptyStack), emptyStack, capacity, onChangedCallback);
    }

    public StackListHandler(NonNullList<S> stacks, S emptyStack, int capacity, @Nullable Runnable onChangedCallback) {
        this.capacity = capacity;
        this.stacks = stacks;
        this.size = stacks.size();
        this.snapshotJournals.ensureCapacity(size);
        this.onChangeJournal = SetChangedSnapshot.of(onChangedCallback);
        this.emptyStack = emptyStack;
        for (int i = 0; i < size; i++) {
            snapshotJournals.add(new StackJournal(i));
        }
    }

    public abstract R getResourceFrom(S stack);

    public abstract int getAmountFrom(S stack);

    public int getCapacityFrom(R stack) {
        return Integer.MAX_VALUE;
    }

    public abstract boolean isStackEmpty(S stack);

    public abstract boolean matches(R resource, S stack);

    public abstract S toStack(R resource, int amount);

    public abstract S copyOf(S stack);

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
        int capacity = getCapacity(index, resource);

        int inserted, newAmount;
        if (isStackEmpty(currentStack)) {
            //the specified index is empty
            inserted = Math.min(capacity, amount);
            newAmount = inserted;
        } else {
            //is there an item in the specified index already?
            if (!matches(resource, currentStack)) return 0;

            int currentStackAmount = getAmountFrom(currentStack);
            inserted = Math.min(capacity - currentStackAmount, amount);
            newAmount = currentStackAmount + inserted;
        }

        if (inserted > 0) {
            snapshotJournals.get(index).updateSnapshots(transaction);
            set(index, resource, newAmount);
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
            set(index, resource, currentAmount - handledAmount);
        }
        return handledAmount;
    }

    public void set(int index, R resource, int amount) {
        stacks.set(index, toStack(resource, amount));
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
            return copyOf(original);
        }

        @Override
        protected void revertToSnapshot(S snapshot) {
            stacks.set(index, snapshot);
        }

        @Override
        protected void onCommit(S originalState) {
            onChangeJournal.runCallback();
        }
    }
}
