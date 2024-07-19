/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import java.util.stream.IntStream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * A basic implementation of {@link IItemHandlerModifiable}, representing a basic inventory with continuous indices and
 * slot limit of {@link Item#ABSOLUTE_MAX_STACK_SIZE} (99), accepts any {@link ItemStack}.
 * <p>
 * Also implements {@link INBTSerializable} to support {@linkplain net.neoforged.neoforge.attachment.AttachmentType serializable attachment} usecase.
 */
public class ItemStackHandler implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {
    /**
     * The backing list of {@link ItemStack}.
     */
    protected NonNullList<ItemStack> stacks;
    /**
     * The backing list of {@link IItemHandler.Slot}, the size should be kept the same as {@link #stacks}.
     */
    protected NonNullList<Slot> slots;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        this(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    public ItemStackHandler(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
        this.slots = NonNullList.copyOf(IntStream.range(0, stacks.size()).mapToObj(Slot::new).toList());
    }

    /**
     * Set the {@link #stacks} with a new list of {@link ItemStack#EMPTY} with specified size.
     * Also updates {@link #slots} to the new size for consistency.
     * 
     * @param size the size of the new list
     */
    public void setSize(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        final int slots = this.slots.size();
        // The slots list also needs to update if new size is different
        if (slots > size) {
            // Replace the slots list with a smaller sublist if new size is smaller, avoids creating new slot instances
            this.slots = NonNullList.copyOf(this.slots.subList(0, size));
        } else if (slots < size) {
            // Add new slots if new size is bigger, avoids creating new slots list
            for (int i = slots; i < size; ++i) {
                this.slots.add(i, new Slot(i));
            }
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (!isItemValid(slot, stack))
            return stack;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return existing.copyWithCount(toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
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
        onLoad();
    }

    @Override
    public Slot getSlot(int slot) {
        validateSlotIndex(slot);
        return this.slots.get(slot);
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    protected void onLoad() {}

    protected void onContentsChanged(int slot) {}

    public class Slot implements IItemHandler.Slot {
        private final int index;

        protected Slot(int index) {
            this.index = index;
        }

        @Override
        public boolean test(ItemStack stack) {
            return ItemStackHandler.this.isItemValid(index, stack);
        }

        @Override
        public ItemStack get() {
            return ItemStackHandler.this.getStackInSlot(index);
        }

        @Override
        public void set(ItemStack stack) {
            ItemStackHandler.this.setStackInSlot(index, stack);
        }

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            return ItemStackHandler.this.insertItem(index, stack, simulate);
        }

        @Override
        public ItemStack extract(int amount, boolean simulate) {
            return ItemStackHandler.this.extractItem(index, amount, simulate);
        }

        @Override
        public int limit() {
            return ItemStackHandler.this.getSlotLimit(index);
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public IItemHandler handler() {
            return ItemStackHandler.this;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (!(other instanceof IItemHandler.Slot slot))
                return false;
            return ItemStackHandler.this == slot.handler() && index == slot.index();
        }

        @Override
        public int hashCode() {
            return ItemStackHandler.this.hashCode() + index * 31;
        }
    }
}
