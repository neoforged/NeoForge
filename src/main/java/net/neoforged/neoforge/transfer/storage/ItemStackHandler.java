/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.storage;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemStackHandler implements Storage<ItemResource>, INBTSerializable<CompoundTag> {
    protected NonNullList<ItemStack> stacks;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public ItemStackHandler(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

//    @Override
//    public void setStackInSlot(int slot, ItemStack stack) {
//        validateSlotIndex(slot);
//        this.stacks.set(slot, stack);
//        onContentsChanged(slot);
//    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public ItemResource getResource(int index) {
        validateSlotIndex(index);
        return ItemResource.of(this.stacks.get(index));
    }

    @Override
    public int getAmount(int index) {
        validateSlotIndex(index);
        return this.stacks.get(index).getCount();
    }

    @Override
    public int insert(int slot, ItemResource resource, int maxAmount, TransferAction action) {
        if (resource.isBlank())
            return 0;

        // TODO: toStack allocates here
        if (!isItemValid(slot, resource.toStack()))
            return 0;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        // TODO: allocates
        int limit = getStackLimit(slot, resource.toStack());

        if (!existing.isEmpty()) {
            if (!resource.matches(existing))
                return 0;

            limit -= existing.getCount();
        }

        int inserted = Math.min(maxAmount, limit);

        if (inserted > 0) {
            if (action.isExecuting()) {
                if (existing.isEmpty()) {
                    this.stacks.set(slot, resource.toStack(inserted));
                } else {
                    existing.grow(inserted);
                }
                onContentsChanged(slot);
            }
            return inserted;
        }

        return 0;
    }

    @Override
    public int extract(int slot, ItemResource resource, int maxAmount, TransferAction action) {
        if (maxAmount == 0)
            return 0;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty() || !resource.matches(existing))
            return 0;

        int toExtract = Math.min(maxAmount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (action.isExecuting()) {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
        } else {
            if (action.isExecuting()) {
                this.stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                onContentsChanged(slot);
            }
        }

        return toExtract;
    }

    @Override
    public int getCapacity(int index) {
        // TODO
        return getSlotLimit(index);
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        // TODO
        return getStackLimit(index, resource.toStack());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        // TODO
        return isItemValid(index, resource.toStack());
    }

    public int getSlotLimit(int index) {
        return Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

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
        setSize(nbt.getIntOr("Size", stacks.size()));
        nbt.getListOrEmpty("Items").compoundStream().forEach(itemTags -> {
            int slot = itemTags.getIntOr("Slot", -1);

            if (slot >= 0 && slot < stacks.size()) {
                ItemStack.parse(provider, itemTags).ifPresent(stack -> stacks.set(slot, stack));
            }
        });
        onLoad();
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    protected void onLoad() {}

    protected void onContentsChanged(int slot) {}
}
