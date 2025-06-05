/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item.base;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;
import net.neoforged.neoforge.transfer.initem.InItemStorageContext;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Variant of {@code Storage<ItemVariant>} for use with data components.
 * <p>
 * The actual data storage is managed by a data component, and all changes will write back to that component.
 * <p>
 * To use this class, register a new {@link DataComponentType} which holds an {@link ItemContainerContents} for your item.
 * Then reference that component from your {@link ICapabilityProvider} passed to {@link RegisterCapabilitiesEvent#registerItem} to create an instance of this class.
 * <p>
 * Since data components are immutable, this will not work nicely with vanilla's container methods which expect the stack to be mutable.
 * Use {@link ItemHandlerCopySlot} to get around this issue.
 * 
 * @implNote All functions in this class should attempt to minimize component read/writes to avoid unnecessary churn, noting that the component can never be cached.
 */
// TODO: we need to multiply returns by the current amount in the context
// TODO: we need to check that the underlying item did not change
public class ComponentItemStorage implements Storage<ItemVariant> {
    protected final InItemStorageContext context;
    protected final DataComponentType<ItemContainerContents> component;
    protected final int size;

    /**
     * Creates a new {@link ComponentItemStorage} with target size. If the existing component is smaller than the given size, it will be expanded on write.
     *
     * @param context   The storage context representing the location of this item stack
     * @param component The data component referencing the stored inventory of the item stack
     * @param size      The number of slots. Must be less than 256 due to limitations of {@link ItemContainerContents}
     */
    public ComponentItemStorage(InItemStorageContext context, DataComponentType<ItemContainerContents> component, int size) {
        this.context = context;
        this.component = component;
        this.size = size;
        Preconditions.checkArgument(size <= 256, "The max size of ItemContainerContents is 256 slots.");
    }

    public ItemStack getStackInSlot(int slot) {
        ItemContainerContents contents = this.getContents();
        return this.getStackFromContents(contents, slot);
    }

    // TODO: not sure what to do here, we need a transaction to do the exchange
//    public void setStackInSlot(int slot, ItemStack stack) {
//        this.validateSlotIndex(slot);
//        if (!this.isItemValid(slot, stack)) {
//            throw new RuntimeException("Invalid stack " + stack + " for slot " + slot + ")");
//        }
//        ItemContainerContents contents = this.getContents();
//        ItemStack existing = this.getStackFromContents(contents, slot);
//        if (!ItemStack.matches(stack, existing)) {
//            this.updateContents(contents, stack, slot);
//        }
//    }

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
        StoragePreconditions.checkSlot(slot, size);
        return Math.max(getSlotCapacity(slot), resource.getMaxStackSize());
    }

    /**
     * Controls which stacks are allowed in this storage.
     *
     * <p>By default, {@link Item#canFitInsideContainerItems()} is checked. Can be overridden to change this.
     */
    @Override
    public boolean isValid(int slot, ItemVariant resource) {
        StoragePreconditions.checkSlot(slot, size);
        return resource.getItem().canFitInsideContainerItems();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isResourceBlank(int slot) {
        this.validateSlotIndex(slot);
        return getStackInSlot(slot).isEmpty();
    }

    @Override
    public ItemVariant getResource(int slot) {
        this.validateSlotIndex(slot);
        return ItemVariant.of(getStackInSlot(slot));
    }

    @Override
    public long getAmount(int slot) {
        this.validateSlotIndex(slot);
        return getStackInSlot(slot).getCount();
    }

    @Override
    public long insert(int slot, ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.checkSlot(slot, size);
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

        if (!this.isValid(slot, insertedVariant)) {
            return 0;
        }

        ItemContainerContents contents = getContents();
        ItemStack currentStack = getStackFromContents(contents, slot);

        if (insertedVariant.matches(currentStack) || currentStack.isEmpty()) {
            int insertedAmount = Ints.saturatedCast(Math.min(maxAmount, getCapacity(slot, insertedVariant) - currentStack.getCount()));

            if (insertedAmount > 0) {
                if (currentStack.isEmpty()) {
                    currentStack = insertedVariant.toStack(insertedAmount);
                } else {
                    currentStack.grow(insertedAmount);
                }
                var currentState = this.context.getCurrent();
                var updatedVariant = updateContents(currentState, contents, currentStack, slot);

                if (context.exchange(updatedVariant, 1, transaction) == 1) {
                    return insertedAmount;
                }
            }
        }

        return 0;
    }

    @Override
    public long extract(int slot, ItemVariant variant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.checkSlot(slot, size);
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);

        ItemContainerContents contents = this.getContents();
        ItemStack currentStack = this.getStackFromContents(contents, slot);

        if (variant.matches(currentStack)) {
            int extracted = (int) Math.min(currentStack.getCount(), maxAmount);

            if (extracted > 0) {
                currentStack.shrink(extracted);
                var currentState = this.context.getCurrent();
                var updatedVariant = updateContents(currentState, contents, currentStack, slot);

                if (context.exchange(updatedVariant, 1, transaction) == 1) {
                    return extracted;
                }
            }
        }

        return 0;
    }

//    /**
//     * Called from {@link #updateContents} after the stack stored in a slot has been updated.
//     * <p>
//     * Modifications to the stacks used as parameters here will not write-back to the stored data.
//     *
//     * @param slot     The slot that changed
//     * @param oldStack The old stack that was present in the slot
//     * @param newStack The new stack that is now present in the slot
//     */
//    // TODO: not sure what to do with this, it would need to be transactional
//    protected void onContentsChanged(int slot, ItemStack oldStack, ItemStack newStack) {}

    /**
     * Retrieves the current {@link ItemContainerContents}.
     */
    protected ItemContainerContents getContents() {
        if (this.context.getCurrentAmount() == 0) {
            return ItemContainerContents.EMPTY;
        }
        return this.context.getCurrent().getOrDefault(this.component, ItemContainerContents.EMPTY);
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
        this.validateSlotIndex(slot);
        return contents.getSlots() <= slot ? ItemStack.EMPTY : contents.getStackInSlot(slot);
    }

    /**
     * Updates the item variant with the new state of the underlying data component.
     * <p>
     * If the existing component is larger than {@link #size()}, additional slots will <b>not</b> be truncated.
     * 
     * @param contents The existing contents from {@link #getContents()}
     * @param stack    The new stack to set to the slot
     * @param slot     The target slot
     */
    protected ItemVariant updateContents(ItemVariant currentState, ItemContainerContents contents, ItemStack stack, int slot) {
        this.validateSlotIndex(slot);
        // Use the max of the contents slots and the capability slots to avoid truncating
        NonNullList<ItemStack> list = NonNullList.withSize(Math.max(contents.getSlots(), size), ItemStack.EMPTY);
        contents.copyInto(list);
        list.set(slot, stack);
        var newStack = currentState.toStack();
        newStack.set(this.component, ItemContainerContents.fromItems(list));
        return ItemVariant.of(newStack);
    }

    /**
     * Throws {@link IndexOutOfBoundsException} if the provided slot index is invalid.
     */
    protected final void validateSlotIndex(int slot) {
        StoragePreconditions.checkSlot(slot, size);
    }
}
