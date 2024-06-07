/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.TransferAction;

@Deprecated(forRemoval = true, since = "1.22")
public interface IItemHandler extends IResourceHandler<ItemResource> {
    /**
     * Returns the number of slots available
     *
     * @return The number of slots available
     **/
    int getSlotCount();

    @Override
    default int size() {
        return getSlotCount();
    }

    /**
     * Returns the ItemStack in a given slot.
     *
     * The result's stack size may be greater than the itemstack's max size.
     *
     * If the result is empty, then the slot is empty.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This ItemStack <em>MUST NOT</em> be modified. This method is not for
     * altering an inventory's contents. Any implementers who are able to detect
     * modification through this method should throw an exception.
     * </p>
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK</em></strong>
     * </p>
     *
     * @param slot Slot to query
     * @return ItemStack in given slot. Empty Itemstack if the slot is empty.
     **/
    ItemStack getStackInSlot(int slot);

    @Override
    default ItemResource getResource(int index) {
        return ItemResource.of(getStackInSlot(index));
    }

    @Override
    default int getAmount(int index) {
        return getStackInSlot(index).getCount();
    }

    /**
     * <p>
     * Inserts an ItemStack into the given slot and return the remainder.
     * The ItemStack <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param slot     Slot to insert into.
     * @param stack    ItemStack to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     *         May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     *         The returned ItemStack can be safely modified after.
     **/
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    @Override
    default int insert(int index, ItemResource resource, int amount, TransferAction action) {
        return amount - insertItem(index, resource.toStack(amount), action.isSimulating()).getCount();
    }

    @Override
    default int insert(ItemResource resource, int amount, TransferAction action) {
        int inserted = 0;
        for (int i = 0; i < this.getSlotCount(); i++) {
            inserted += insert(i, resource, amount - inserted, action);
            if (inserted >= amount) {
                break;
            }
        }
        return inserted;
    }

    /**
     * Extracts an ItemStack from the given slot.
     * <p>
     * The returned value must be empty if nothing is extracted,
     * otherwise its stack size must be less than or equal to {@code amount} and {@link ItemStack#getMaxStackSize()}.
     * </p>
     *
     * @param slot     Slot to extract from.
     * @param amount   Amount to extract (may be greater than the current stack's max limit)
     * @param simulate If true, the extraction is only simulated
     * @return ItemStack extracted from the slot, must be empty if nothing can be extracted.
     *         The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
     **/
    ItemStack extractItem(int slot, int amount, boolean simulate);

    @Override
    default int extract(int index, ItemResource resource, int amount, TransferAction action) {
        if (!resource.matches(getStackInSlot(index))) return 0;
        return extractItem(index, amount, action.isSimulating()).getCount();
    }

    @Override
    default int extract(ItemResource resource, int amount, TransferAction action) {
        int extracted = 0;
        for (int i = 0; i < this.getSlotCount(); i++) {
            extracted += extract(i, resource, amount - extracted, action);
            if (extracted >= amount) {
                break;
            }
        }
        return extracted;
    }

    /**
     * Retrieves the maximum stack size allowed to exist in the given slot.
     *
     * @param slot Slot to query.
     * @return The maximum stack size allowed in the slot.
     */
    int getSlotLimit(int slot);

    @Override
    default int getLimit(int index, ItemResource resource) {
        return getSlotLimit(index);
    }

    /**
     * <p>
     * This function re-implements the vanilla function {@link Container#canPlaceItem(int, ItemStack)}.
     * It should be used instead of simulated insertions in cases where the contents and state of the inventory are
     * irrelevant, mainly for the purpose of automation and logic (for instance, testing if a minecart can wait
     * to deposit its items into a full inventory, or if the items in the minecart can never be placed into the
     * inventory and should move on).
     * </p>
     * <ul>
     * <li>isItemValid is false when insertion of the item is never valid.</li>
     * <li>When isItemValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual items in the inventory, its fullness, or any other state are <strong>not</strong> considered by isItemValid.</li>
     * </ul>
     * 
     * @param slot  Slot to query for validity
     * @param stack Stack to test with for validity
     *
     * @return true if the slot can insert the ItemStack, not considering the current state of the inventory.
     *         false if the slot can never insert the ItemStack in any situation.
     */
    boolean isItemValid(int slot, ItemStack stack);

    @Override
    default boolean isValid(int index, ItemResource resource) {
        return isItemValid(index, resource.toStack(1));
    }

    @Override
    default boolean canInsert() {
        return true;
    }

    @Override
    default boolean canExtract() {
        return true;
    }
}
