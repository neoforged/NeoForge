/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

/**
 * Variant of {@link ItemStackHandler} for use with data components.
 * <p>
 * The actual data storage is managed by a data component, and all changes will write back to that component.
 * <p>
 * To use this class, register a new {@link DataComponentType} which holds an {@link ItemContainerContents} for your item.
 * Then reference that component from your {@link ICapabilityProvider} passed to {@link RegisterCapabilitiesEvent#registerItem} to create an instance of this class.
 * 
 * @implNote All functions in this class should attempt to minimize component read/writes to avoid unnecessary churn, noting that the component can never be cached.
 */
public class ComponentItemHandler implements IItemHandlerModifiable {
    protected final MutableDataComponentHolder parent;
    protected final DataComponentType<ItemContainerContents> component;
    protected final int size;

    /**
     * Creates a new {@link ComponentItemHandler} with target size. If the existing component is smaller than the given size, it will be expanded on write.
     * 
     * @param parent    The parent component holder, such as an {@link ItemStack}
     * @param component The data component referencing the stored inventory of the item stack
     * @param size      The number of slots. Must be less than 256 due to limitations of {@link ItemContainerContents}
     */
    public ComponentItemHandler(MutableDataComponentHolder parent, DataComponentType<ItemContainerContents> component, int size) {
        this.parent = parent;
        this.component = component;
        this.size = size;
        Preconditions.checkArgument(size <= 256, "The max size of ItemContainerContents is 256 slots.");
    }

    @Override
    public int getSlots() {
        return this.size;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemContainerContents contents = this.getContents();
        return this.getStackFromContents(contents, slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack toInsert, boolean simulate) {
        this.validateSlotIndex(slot);

        if (toInsert.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!this.isItemValid(slot, toInsert)) {
            return toInsert;
        }

        ItemContainerContents contents = this.getContents();
        ItemStack existing = this.getStackFromContents(contents, slot);
        // Max amount of the stack that could be inserted
        int insertLimit = Math.min(this.getSlotLimit(slot), toInsert.getMaxStackSize());

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(toInsert, existing)) {
                return toInsert;
            }

            insertLimit -= existing.getCount();
        }

        if (insertLimit <= 0) {
            return toInsert;
        }

        int inserted = Math.min(insertLimit, toInsert.getCount());

        if (!simulate) {
            this.updateContents(contents, toInsert.copyWithCount(existing.getCount() + inserted), slot);
        }

        return toInsert.copyWithCount(toInsert.getCount() - inserted);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot);

        if (amount == 0) {
            return ItemStack.EMPTY;
        }

        ItemContainerContents contents = this.getContents();
        ItemStack existing = getStackFromContents(contents, slot);

        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (!simulate) {
            updateContents(contents, existing.copyWithCount(existing.getCount() - toExtract), slot);
        }

        return existing.copyWithCount(toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.getItem().canFitInsideContainerItems();
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        if (!isItemValid(slot, stack)) {
            throw new RuntimeException("Invalid stack " + stack + " for slot " + slot + ")");
        }
        updateContents(getContents(), stack, slot);
    }

    /**
     * Retrieves the {@link ItemContainerContents} from the parent object's data component map.
     */
    protected ItemContainerContents getContents() {
        return this.parent.getOrDefault(this.component, ItemContainerContents.EMPTY);
    }

    /**
     * Retrieves a copy of a single stack from the underlying data component, returning {@link ItemStack#EMPTY} if the component does not have a slot present.
     * <p>
     * Throws an exception if the slot is out-of-bounds for this capability.
     * 
     * @param contents The existing contents from {@link #getContents()}
     * @param slot     The target slot
     * @return A copy of the stack in the target slot
     */
    protected ItemStack getStackFromContents(ItemContainerContents contents, int slot) {
        validateSlotIndex(slot);
        return contents.getSlots() <= slot ? ItemStack.EMPTY : contents.getStackInSlot(slot);
    }

    /**
     * Performs a copy and write operation on the underlying data component, changing the stack in the target slot.
     * <p>
     * If the existing component is larger than {@link #getSlots()}, additional slots will <b>not</b> be truncated.
     * 
     * @param contents The existing contents from {@link #getContents()}
     * @param stack    The new stack to set to the slot
     * @param slot     The target slot
     */
    protected void updateContents(ItemContainerContents contents, ItemStack stack, int slot) {
        validateSlotIndex(slot);
        // Use the max of the contents slots and the capability slots to avoid truncating
        NonNullList<ItemStack> list = NonNullList.withSize(Math.max(contents.getSlots(), this.getSlots()), ItemStack.EMPTY);
        contents.copyInto(list);
        list.set(slot, stack);
        this.parent.set(this.component, ItemContainerContents.fromItems(list));
    }

    /**
     * Throws {@link UnsupportedOperationException} if the provided slot index is invalid.
     */
    protected final void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
        }
    }
}
