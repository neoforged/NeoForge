/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import java.util.Objects;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;

/**
 * @deprecated Use {@link ResourceHandler} with an {@link ItemResource} instead. Code that is written against {@link IItemHandler} but receives
 *             a {@code ResourceHandler<ItemResource>} can temporarily use {@link IItemHandler#of} to ease migration.
 */
@Deprecated(since = "1.21.9", forRemoval = true)
public interface IItemHandler {
    /**
     * Creates a wrapper around an item {@link ResourceHandler}, to present it as a legacy {@link IItemHandler}.
     *
     * <p>This class is intended to make migration easier for code that expects an {@link IItemHandler}.
     *
     * @apiNote The {@link #insertItem} and {@link #extractItem} implementations will open new root transactions,
     *          so this wrapper cannot be used from a transactional context (such as {@link ResourceHandler#insert}).
     */
    static IItemHandler of(ResourceHandler<ItemResource> handler) {
        Objects.requireNonNull(handler, "handler");

        return new ItemResourceHandlerAdapter(handler);
    }

    /**
     * Returns the number of slots available
     *
     * @return The number of slots available
     * @deprecated Use {@link ResourceHandler#size()} instead.
     **/
    @Deprecated(since = "1.21.9", forRemoval = true)
    int getSlots();

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
     * @deprecated Use {@link ResourceHandler#getResource} and {@link ResourceHandler#getAmountAsInt} instead.
     *             Alternatively use the {@link ItemUtil#getStack} helper.
     **/
    @Deprecated(since = "1.21.9", forRemoval = true)
    ItemStack getStackInSlot(int slot);

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
     * @deprecated Use {@link ResourceHandler#insert} instead.
     *             Note that {@link ResourceHandler#insert} returns <strong>how much was inserted</strong>,
     *             unlike this method which returns the leftover (i.e. how much was <strong>not</strong> inserted).
     *             Alternatively use the {@link ItemUtil#insertItemReturnRemaining} helper.
     **/
    @Deprecated(since = "1.21.9", forRemoval = true)
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

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
     * @deprecated Use {@link ResourceHandler#extract} instead.
     **/
    @Deprecated(since = "1.21.9", forRemoval = true)
    ItemStack extractItem(int slot, int amount, boolean simulate);

    /**
     * Retrieves the maximum stack size allowed to exist in the given slot.
     *
     * @param slot Slot to query.
     * @return The maximum stack size allowed in the slot.
     * @deprecated Use {@link ResourceHandler#getCapacityAsInt} instead,
     *             passing {@link ItemResource#EMPTY} as the resource to retrieve a general slot limit.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    int getSlotLimit(int slot);

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
     * @deprecated Use {@link ResourceHandler#isValid} instead, however note that it doesn't make the same strong guarantees
     *             regarding how long a resource is valid. In other words: the result of {@code isValid} might change.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    boolean isItemValid(int slot, ItemStack stack);
}
