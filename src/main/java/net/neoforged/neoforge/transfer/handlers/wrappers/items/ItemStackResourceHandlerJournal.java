/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A single-index item snapshot backed by an {@link ItemStack}.
 * Implementors should at least override {@link #get} and {@link #set},
 * and probably {@link #onCommit} as well for {@code markDirty()} and similar calls.
 *
 * <p>{@link #canInsert} and {@link #canExtract} can be used for more precise control over which items may be inserted or extracted.
 * {@link #getCapacity(ItemResource)} can be overridden to change the maximum capacity depending on the item resource.
 */
public abstract class ItemStackResourceHandlerJournal extends SnapshotJournal<ItemStack> implements ISingleResourceHandler<ItemResource> {
    /**
     * Return the stack of this storage. It will be modified directly sometimes to avoid needless copies.
     * However, any mutation of the stack will directly be followed by a call to {@link #set}.
     * This means that either returning the backing stack directly or a copy is safe.
     *
     * @return The current stack.
     */
    protected abstract ItemStack get();

    /**
     * Set the stack of this storage.
     */
    protected abstract void set(ItemStack stack);

    /**
     * Return {@code true} if the passed non-blank item resource can be inserted, {@code false} otherwise.
     */
    protected boolean canInsert(ItemResource resource) {
        return true;
    }

    /**
     * Return {@code true} if the passed non-blank item resource can be extracted, {@code false} otherwise.
     */
    protected boolean canExtract(ItemResource resource) {
        return true;
    }

    /**
     * Return the maximum capacity of this storage for the passed item resource.
     * If the passed item resource is blank, an estimate should be returned.
     *
     * <p>If the capacity should be limited by the max stack size of the item, this function must take it into account.
     * For example, a storage with a maximum count of 4, or less for items that have a smaller max stack size,
     * should override this to return {@code Math.min(resource.getMaxStackSize(), 4);}.
     *
     * @return The maximum capacity of this storage for the passed item resource.
     */
    protected int getCapacity(ItemResource resource) {
        return resource.getMaxStackSize();
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        ItemStack currentStack = get();

        if ((!currentStack.isEmpty() && !resource.is(currentStack)) || !canInsert(resource)) return 0;

        int insertedAmount = Math.min(amount, getCapacity(resource) - currentStack.getCount());
        if (insertedAmount == 0) return 0;

        updateSnapshots(transaction);
        currentStack = get();

        if (currentStack.isEmpty()) {
            currentStack = resource.toStack(insertedAmount);
        } else {
            currentStack.grow(insertedAmount);
        }

        set(currentStack);

        return insertedAmount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        ItemStack currentStack = get();

        if (!resource.is(currentStack) || !canExtract(resource)) return 0;

        int extracted = Math.min(currentStack.getCount(), amount);
        if (extracted == 0) return 0;

        this.updateSnapshots(transaction);
        currentStack = get();
        currentStack.shrink(extracted);
        set(currentStack);

        return extracted;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());
        return ItemResource.of(get());
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return get().getCount();
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return getCapacity(resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return canInsert(resource);
    }

    @Override
    protected ItemStack createSnapshot() {
        ItemStack original = get();
        set(original.copy());
        return original;
    }

    @Override
    protected void revertToSnapshot(ItemStack snapshot) {
        set(snapshot);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + get() + "]";
    }
}
