/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A <strong>single-index</strong> item resource handler, backed by an {@link ItemStack}.
 * Implementors should at least override {@link #getStack} and {@link #setStack},
 * and probably {@link #onRootCommit} as well for {@code setChanged()} and similar calls.
 *
 * <p>{@link #isValid} can be used for more precise control over which items may be stored.
 * {@link #getCapacity(ItemResource)} can be overridden to change the maximum capacity depending on the item resource.
 */
public abstract class ItemStackResourceHandler extends SnapshotJournal<ItemStack> implements ResourceHandler<ItemResource> {
    /**
     * Return the stack of this handler. It will be modified directly sometimes to avoid needless copies.
     * However, any mutation of the stack will directly be followed by a call to {@link #setStack}.
     * This means that either returning the backing stack directly or a copy is safe.
     * If returning a copy, consider overriding {@link #getAmountAsLong} and {@link #getResource} to avoid unnecessary copies.
     *
     * @return The current stack.
     */
    protected abstract ItemStack getStack();

    /**
     * Set the stack of this handler.
     */
    protected abstract void setStack(ItemStack stack);

    /**
     * Return {@code true} if the passed non-empty item resource can fit in this handler, {@code false} otherwise.
     *
     * <p>The result of this function is used in the provided implementations of:
     * <ul>
     * <li>{@link #isValid(int, ItemResource)}, to report which items are valid;</li>
     * <li>{@link #getCapacityAsLong(int, ItemResource)}, to report a capacity of {@code 0} for invalid items;</li>
     * <li>{@link #insert(int, ItemResource, int, TransactionContext)}, to reject items that cannot fit in this handler.</li>
     * </ul>
     */
    protected boolean isValid(ItemResource resource) {
        return true;
    }

    /**
     * Return the maximum capacity of this handler for the passed item resource.
     * If the passed item resource is empty, an estimate should be returned.
     *
     * <p>If the capacity should be limited by the max stack size of the item, this function must take it into account.
     * Additionally, the empty resource should be special-cased to return the intended maximum capacity of the handler,
     * as it will otherwise report a {@linkplain ItemResource#getMaxStackSize() max stack size} of 1.
     * For example, a handler with a maximum count of 4, or less for items that have a smaller max stack size,
     * should override this to return {@code resource.isEmpty() ? 4 : Math.min(resource.getMaxStackSize(), 4)}.
     *
     * @return The maximum capacity of this handler for the passed item resource.
     */
    protected int getCapacity(ItemResource resource) {
        return resource.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    @Override
    public final int size() {
        return 1;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        ItemStack currentStack = getStack();

        if ((currentStack.isEmpty() || resource.matches(currentStack)) && isValid(resource)) {
            int insertedAmount = Math.min(amount, getCapacity(resource) - currentStack.getCount());

            if (insertedAmount > 0) {
                updateSnapshots(transaction);
                currentStack = getStack();

                if (currentStack.isEmpty()) {
                    currentStack = resource.toStack(insertedAmount);
                } else {
                    currentStack.grow(insertedAmount);
                }

                setStack(currentStack);
                return insertedAmount;
            }
        }

        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        ItemStack currentStack = getStack();

        if (resource.matches(currentStack)) {
            int extracted = Math.min(currentStack.getCount(), amount);

            if (extracted > 0) {
                this.updateSnapshots(transaction);
                currentStack = getStack();
                currentStack.shrink(extracted);
                setStack(currentStack);

                return extracted;
            }
        }

        return 0;
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());
        return ItemResource.of(getStack());
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return getStack().getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty() || isValid(resource) ? getCapacity(resource) : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmpty(resource);
        return isValid(resource);
    }

    @Override
    protected ItemStack createSnapshot() {
        ItemStack original = getStack();
        setStack(original.copy());
        return original;
    }

    @Override
    protected void revertToSnapshot(ItemStack snapshot) {
        setStack(snapshot);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + getStack() + "]";
    }
}
