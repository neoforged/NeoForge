/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public interface IItemHandler extends Iterable<IItemHandler.Slot> {
    /**
     * Returns the number of slots available
     *
     * @return The number of slots available
     **/
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
     **/
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
     **/
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
     **/
    ItemStack extractItem(int slot, int amount, boolean simulate);

    /**
     * Retrieves the maximum stack size allowed to exist in the given slot.
     *
     * @param slot Slot to query.
     * @return The maximum stack size allowed in the slot.
     */
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
     */
    boolean isItemValid(int slot, ItemStack stack);

    /**
     * Returns a {@link Slot} of a specific index. The default implementation always return a new {@link Slot} obejct,
     * however it's encouraged for implementation to return existing {@link Slot} (from list) for better performance.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= getSlots()})
     * @param index the index of the slot
     * @return the {@link Slot} of the index.
     */
    default Slot getSlot(int index) {
        Preconditions.checkElementIndex(index, this.getSlots());
        return new Slot() {
            @Override
            public boolean test(ItemStack stack) {
                return IItemHandler.this.isItemValid(index, stack);
            }

            @Override
            public ItemStack get() {
                return IItemHandler.this.getStackInSlot(index);
            }

            @Override
            public ItemStack insert(ItemStack stack, boolean simulate) {
                return IItemHandler.this.insertItem(index, stack, simulate);
            }

            @Override
            public ItemStack extract(int amount, boolean simulate) {
                return IItemHandler.this.extractItem(index, amount, simulate);
            }

            @Override
            public int limit() {
                return IItemHandler.this.getSlotLimit(index);
            }

            @Override
            public int index() {
                return index;
            }

            @Override
            public IItemHandler handler() {
                return IItemHandler.this;
            }

            @Override
            public boolean equals(Object other) {
                if (this == other)
                    return true;
                if (!(other instanceof Slot slot))
                    return false;
                return IItemHandler.this == slot.handler() && index == slot.index();
            }

            @Override
            public int hashCode() {
                return IItemHandler.this.hashCode() + index * 31;
            }
        };
    }

    /**
     * @return an {@link Iterator} of the {@link Slot slots}, the default implementation expects continuous slot indices,
     *         and has no check for concurrent modification.
     */
    default Iterator<Slot> iterator() {
        return new Iterator<>() {
            private int slot = 0;

            @Override
            public boolean hasNext() {
                return this.slot < IItemHandler.this.getSlots();
            }

            @Override
            public Slot next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return IItemHandler.this.getSlot(this.slot++);
            }
        };
    }

    /**
     * Creates a {@link Spliterator} of the {@link Slot slots}.
     * <p>
     * The {@code Spliterator} reports {@link Spliterator#SIZED}, {@link Spliterator#ORDERED}, {@link Spliterator#NONNULL} and {@link Spliterator#IMMUTABLE}.
     * <p>
     * Implementations should document the reporting of additional characteristic values.
     * 
     * @return a {@link Spliterator} of the {@link Slot slots}.
     */
    @Override
    default Spliterator<Slot> spliterator() {
        return Spliterators.spliterator(iterator(), this.getSlots(), Spliterator.SIZED | Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
    }

    /**
     * @return a sequenced {@link Stream} of the {@link Slot slots}.
     */
    default Stream<Slot> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Interface representing a slot of the {@link IItemHandler}.
     */
    interface Slot extends Supplier<ItemStack>, Predicate<ItemStack> {
        /**
         * @see IItemHandler#isItemValid(int, ItemStack)
         */
        @Override
        boolean test(ItemStack stack);

        /**
         * @see IItemHandler#getStackInSlot(int)
         */
        @Override
        ItemStack get();

        /**
         * Only supported by {@link IItemHandlerModifiable}.
         * 
         * @see IItemHandlerModifiable#setStackInSlot(int, ItemStack)
         */
        default void set(ItemStack stack) {
            throw new UnsupportedOperationException();
        }

        /**
         * @see IItemHandler#insertItem(int, ItemStack, boolean)
         */
        ItemStack insert(ItemStack stack, boolean simulate);

        /**
         * @see IItemHandler#extractItem(int, int, boolean)
         */
        ItemStack extract(int amount, boolean simulate);

        /**
         * @see IItemHandler#getSlotLimit(int)
         */
        int limit();

        /**
         * @return the index of this slot.
         */
        int index();

        /**
         * @return the {@link IItemHandler} which this slot belongs.
         */
        IItemHandler handler();

        /**
         * @implSpec Returns true if and only if the other {@link Slot} is of the same {@link IItemHandler} object and same index.
         */
        @Override
        boolean equals(Object other);

        /**
         * @implSpec Result must be identical for the same {@link IItemHandler} object and same index.
         */
        @Override
        int hashCode();
    }
}
