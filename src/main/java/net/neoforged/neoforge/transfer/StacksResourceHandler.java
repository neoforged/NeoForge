/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Base implementation of a {@link ResourceHandler} backed by a list of stacks.
 * This implementation is generic in the type of transferred resources {@code T},
 * and in the type of stack {@code S} used to store the contents of the handler.
 *
 * <p>As a result of this flexibility, this base implementation comes with the following methods will typically be overridden:
 * <ul>
 * <li>(required) {@link #getResourceFrom}, {@link #getAmountFrom}, and {@link #getStackFrom} to convert between amounts, resources and stacks.</li>
 * <li>(required) {@link #copyOf} to copy stacks for snapshotting support.</li>
 * <li>(recommended) {@link #matches} to optimize the frequent operation of checking whether a resource and a stack match.</li>
 * <li>(optional) {@link #isValid} to limit which resources are allowed in this handler; by default any resource is allowed.</li>
 * <li>(required) {@link #getCapacity} to specify the capacity of this handler.</li>
 * <li>(recommended) {@link #onContentsChanged} to react to changes in this handler, for example to trigger {@code setChanged()}.</li>
 * </ul>
 *
 * @param <S> The type of stack used to store the contents of this handler.
 * @param <T> The type of resource this handler manages.
 * @see ItemStacksResourceHandler the ItemStack-based subclass
 * @see FluidStacksResourceHandler the FluidStack-based subclass
 * @see ResourceStacksResourceHandler the ResourceStack-based subclass
 */
public abstract class StacksResourceHandler<S, T extends Resource> implements ResourceHandler<T>, ValueIOSerializable {
    public static final String VALUE_IO_KEY = "stacks";

    protected final S emptyStack;
    protected NonNullList<S> stacks;
    protected final Codec<NonNullList<S>> codec;

    private final List<StackJournal> snapshotJournals;

    protected StacksResourceHandler(int size, S emptyStack, Codec<S> stackCodec) {
        this(NonNullList.withSize(size, emptyStack), emptyStack, stackCodec);
    }

    protected StacksResourceHandler(NonNullList<S> stacks, S emptyStack, Codec<S> stackCodec) {
        this.emptyStack = emptyStack;
        this.stacks = mutableCopyOf(stacks);
        // Don't use NonNullList.codecOf because it creates an unmodifiable list
        this.codec = stackCodec.listOf().xmap(this::mutableCopyOf, Function.identity());
        this.snapshotJournals = new ArrayList<>(this.stacks.size());
        for (int i = 0; i < this.stacks.size(); i++) {
            snapshotJournals.add(new StackJournal(i));
        }
    }

    /**
     * Creates a {@link NonNullList} that is a fixed-size mutable copy of the given collection.
     */
    private NonNullList<S> mutableCopyOf(Collection<S> list) {
        return NonNullList.of(emptyStack, (S[]) list.toArray(Object[]::new));
    }

    @Override
    public void serialize(ValueOutput output) {
        output.store(VALUE_IO_KEY, codec, stacks);
    }

    @Override
    public void deserialize(ValueInput input) {
        input.read(VALUE_IO_KEY, codec).ifPresent(l -> {
            stacks = l;
        });
    }

    /**
     * Directly overwrites the contents of the handler.
     *
     * <p>Note that this method can be used as an {@link IndexModifier}, for usage in {@link ResourceHandlerSlot}.
     *
     * @param index    index to change
     * @param resource new resource at the index
     * @param amount   new amount at the index
     * @throws IllegalArgumentException if either the amount is negative; or if the resource is non-empty for a 0 amount
     */
    public void set(int index, T resource, int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (resource.isEmpty() && amount > 0) {
            throw new IllegalArgumentException("Resource is empty but the amount is positive: " + amount);
        }

        S oldContents = stacks.set(index, getStackFrom(resource, amount));
        onContentsChanged(index, oldContents);
    }

    /**
     * Retrieves the resource from a stack.
     * In the case of an {@link ItemStack} an {@link ItemResource} would be returned for example.
     */
    protected abstract T getResourceFrom(S stack);

    /**
     * Retrieves the amount from a stack.
     * In the case of an {@link ItemStack} {@linkplain ItemStack#getCount() the count} would be returned for example.
     */
    protected abstract int getAmountFrom(S stack);

    /**
     * Creates a stack from a resource and an amount.
     *
     * <p>If the stack {@linkplain ResourceHandlerUtil#isEmpty(Resource, int) would be empty},
     * consider returning {@link #emptyStack} instead of creating a new empty stack instance.
     */
    protected abstract S getStackFrom(T resource, int amount);

    /**
     * Creates a copy of a stack, for use as a snapshot.
     *
     * <p>If using an immutable stack type such as {@link ResourceStack}, it can be returned as is.
     * In the case of a mutable stack type such as an item or fluid stack, a copy should be returned.
     */
    protected abstract S copyOf(S stack);

    /**
     * Checks if the passed resource corresponds to the stack.
     *
     * @param stack    the stack, usually the current stored value
     * @param resource the resource, usually the received value in insert or extract
     * @return {@code true} if the stack and resource match; {@code false} otherwise.
     * @implSpec This function should be equivalent to {@code getResourceFrom(stack).equals(resource)}.
     */
    protected boolean matches(S stack, T resource) {
        return getResourceFrom(stack).equals(resource);
    }

    /**
     * Return {@code true} if the passed non-empty resource can fit in this handler, {@code false} otherwise.
     *
     * <p>The result of this function is used in the provided implementations of:
     * <ul>
     * <li>{@link #getCapacityAsLong(int, T)}, to report a capacity of {@code 0} for invalid items;</li>
     * <li>{@link #insert(int, T, int, TransactionContext)}, to reject items that cannot fit in this handler.</li>
     * </ul>
     */
    @Override
    public boolean isValid(int index, T resource) {
        return true;
    }

    /**
     * Return the maximum capacity of this handler for the passed resource.
     * If the passed resource is empty, an estimate should be returned.
     *
     * @return The maximum capacity of this handler for the passed resource.
     */
    protected abstract int getCapacity(int index, T resource);

    /**
     * Called after the contents of the handler changed.
     *
     * <p>For changes that happen through {@link #set}, this method is called immediately.
     * For changes that happen through {@link #insert} or {@link #extract},
     * this function will be called at the end of the transaction, once per index that changed.
     *
     * @param index            the index where the change happened
     * @param previousContents the stack before the change
     */
    protected void onContentsChanged(int index, S previousContents) {}

    /**
     * Copies all the contents of this handler to a mutable fixed-size {@link NonNullList}.
     */
    public NonNullList<S> copyToList() {
        return mutableCopyOf(stacks);
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return getResourceFrom(stacks.get(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return getAmountFrom(stacks.get(index));
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty() || isValid(index, resource) ? getCapacity(index, resource) : 0;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        S currentStack = stacks.get(index);
        int currentAmount = getAmountFrom(currentStack);

        if ((currentAmount == 0 || matches(currentStack, resource)) && isValid(index, resource)) {
            int inserted = Math.min(amount, getCapacity(index, resource) - currentAmount);

            if (inserted > 0) {
                snapshotJournals.get(index).updateSnapshots(transaction);
                stacks.set(index, getStackFrom(resource, currentAmount + inserted));
                return inserted;
            }
        }

        return 0;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        S currentStack = stacks.get(index);

        if (matches(currentStack, resource)) {
            int currentAmount = getAmountFrom(currentStack);
            int extracted = Math.min(amount, currentAmount);

            if (extracted > 0) {
                snapshotJournals.get(index).updateSnapshots(transaction);
                stacks.set(index, getStackFrom(resource, currentAmount - extracted));
                return extracted;
            }
        }

        return 0;
    }

    private class StackJournal extends SnapshotJournal<S> {
        private final int index;

        private StackJournal(int index) {
            this.index = index;
        }

        @Override
        protected S createSnapshot() {
            return copyOf(stacks.get(index));
        }

        @Override
        protected void revertToSnapshot(S snapshot) {
            stacks.set(index, snapshot);
        }

        @Override
        protected void onRootCommit(S originalState) {
            onContentsChanged(index, originalState);
        }
    }
}
