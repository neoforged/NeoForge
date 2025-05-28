/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item.base;

import com.google.common.primitives.Ints;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotParticipant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of {@code Storage<ItemVariant>} that is backed by a list of stacks.
 *
 * <p>Overriding the following methods is an easy way to customize the behavior of this storage:
 * <ul>
 *     <li>{@link #onContentsChanged} to react to changes to react to slot content changes.</li>
 *     <li>{@link #getSlotCapacity} and {@link #getCapacity} to configure the capacity of each slot.</li>
 *     <li>{@link #isValid} to configure which stacks are allowed in the storage.</li>
 * </ul>
 */
public class ItemStackStorage implements Storage<ItemVariant>, INBTSerializable<CompoundTag> {
    private NonNullList<ItemStack> stacks;
    private final List<SlotParticipant> slotParticipants = new ArrayList<>();

    private class SlotParticipant extends SnapshotParticipant<ItemStack> {
        private final int slot;

        private SlotParticipant(int slot) {
            this.slot = slot;
        }

        @Override
        protected ItemStack createSnapshot() {
            return stacks.get(slot).copy();
        }

        @Override
        protected void revertToSnapshot(ItemStack snapshot) {
            stacks.set(slot, snapshot);
        }

        @Override
        protected void onFinalCommit(ItemStack originalState) {
            onContentsChanged(slot);
        }
    }

    /**
     * Creates a new storage that can hold a single stack.
     */
    public ItemStackStorage() {
        this(1);
    }

    /**
     * Creates a new storage with a fixed size.
     */
    public ItemStackStorage(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        updateSlots();
    }

    /**
     * Creates a new storage that will use the passed list as the backing storage for stacks.
     */
    public ItemStackStorage(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
        updateSlots();
    }

    /**
     * Changes the size of this storage.
     *
     * TODO: currently, calling this while this storage is being used inside a transaction is undefined behavior
     */
    public void setSize(int size) {
        // TODO: A bit awkward if called from inside a transaction...
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        updateSlots();
    }

    private void updateSlots() {
        while (slotParticipants.size() < stacks.size()) {
            slotParticipants.add(new SlotParticipant(slotParticipants.size()));
        }
    }

    /**
     * Returns the stack stored in the given slot.
     *
     * <p>Note that the stack is returned directly without copying.
     */
    public ItemStack getStackInSlot(int slot) {
        StoragePreconditions.checkSlot(slot, stacks.size());
        return stacks.get(slot);
    }

    /**
     * Sets the stack at the given slot.
     *
     * <p>Note that the stack is stored directly without copying.
     */
    public void setStackInSlot(int slot, ItemStack stack) {
        StoragePreconditions.checkSlot(slot, stacks.size());
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    /**
     * Called when the contents of a slot changed.
     *
     * <p>Can be overridden to react to perform side effects after slot changes such as calling {@code setChanged}.
     * Note that this function is only called at the end of a successful transaction in which the slot's contents were changed.
     */
    protected void onContentsChanged(int slot) {}

    /**
     * Controls the stack-independent capacity of a slot.
     *
     * <p>Can be overridden to change the max size of some slots.
     * The effective capacity of each slot is the minimum of this value and the max stack size of the inserted stack.
     *
     * @see #getCapacity
     */
    protected int getSlotCapacity(int slot) {
        return Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    /**
     * Controls the capacity of a slot.
     *
     * <p>Can be overridden to change the max size of some slots, in a way that is dependent on the inserted stack.
     * Make sure to manually take into account the {@link ItemVariant#getMaxStackSize() max stack size} of the variant, if that is relevant.
     */
    @Override
    public long getCapacity(int slot, ItemVariant resource) {
        StoragePreconditions.checkSlot(slot, stacks.size());
        return Math.max(getSlotCapacity(slot), resource.getMaxStackSize());
    }

    /**
     * Controls which stacks are allowed in this storage.
     *
     * <p>By default, all stacks are allowed. Can be overridden to change this.
     */
    @Override
    public boolean isValid(int slot, ItemVariant resource) {
        StoragePreconditions.checkSlot(slot, stacks.size());
        return true;
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public boolean isResourceBlank(int slot) {
        StoragePreconditions.checkSlot(slot, stacks.size());
        return this.stacks.get(slot).isEmpty();
    }

    @Override
    public ItemVariant getResource(int slot) {
        StoragePreconditions.checkSlot(slot, stacks.size());
        return ItemVariant.of(this.stacks.get(slot));
    }

    @Override
    public long getAmount(int slot) {
        StoragePreconditions.checkSlot(slot, stacks.size());
        return this.stacks.get(slot).getCount();
    }

    @Override
    public long insert(int slot, ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.checkSlot(slot, stacks.size());
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

        ItemStack currentStack = getStackInSlot(slot);

        if ((insertedVariant.matches(currentStack) || currentStack.isEmpty()) && isValid(slot, insertedVariant)) {
            int insertedAmount = Ints.saturatedCast(Math.min(maxAmount, getCapacity(slot, insertedVariant) - currentStack.getCount()));

            if (insertedAmount > 0) {
                slotParticipants.get(slot).updateSnapshots(transaction);
                if (currentStack.isEmpty()) {
                    currentStack = insertedVariant.toStack(insertedAmount);
                } else {
                    currentStack.grow(insertedAmount);
                }
                stacks.set(slot, currentStack);

                return insertedAmount;
            }
        }

        return 0;
    }

    @Override
    public long extract(int slot, ItemVariant variant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.checkSlot(slot, stacks.size());
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);

        ItemStack currentStack = getStackInSlot(slot);

        if (variant.matches(currentStack)) {
            int extracted = (int) Math.min(currentStack.getCount(), maxAmount);

            if (extracted > 0) {
                slotParticipants.get(slot).updateSnapshots(transaction);
                currentStack.shrink(extracted);
                stacks.set(slot, currentStack);

                return extracted;
            }
        }

        return 0;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                nbtTagList.add(stacks.get(i).save(provider, itemTag));
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                ItemStack.parse(provider, itemTags).ifPresent(stack -> stacks.set(slot, stack));
            }
        }
    }
}
