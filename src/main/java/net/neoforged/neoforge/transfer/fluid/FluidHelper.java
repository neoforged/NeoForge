/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StorageUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Helper functions to work with {@link Storage}s of {@link FluidVariant}s.
 */
public class FluidHelper {
    private FluidHelper() {
    }

    /**
     * Returns the FluidStack in a given tank.
     *
     * <p>If the result is empty, then the slot is empty.
     *
     * @param tank Tank to query
     * @return FluidStack in given Tank. Empty if the slot is empty.
     **/
    public static FluidStack getFluidInTank(Storage<FluidVariant> storage, int tank) {
        FluidVariant resource = storage.getResource(tank);
        if (resource.isBlank()) {
            return FluidStack.EMPTY;
        }
        return resource.toStack(Ints.saturatedCast(storage.getAmount(tank)));
    }

    /**
     * This function is a way to determine which fluids can exist inside a given handler. General purpose tanks will
     * basically always return TRUE for this.
     *
     * @param tank  Tank to query for validity
     * @param stack Stack to test with for validity
     * @return TRUE if the tank can hold the FluidStack, not considering current state.
     * (Basically, is a given fluid EVER allowed in this tank?) Return FALSE if the answer to that question is 'no.'
     */
    public static boolean isFluidValid(Storage<FluidVariant> variant, int tank, FluidStack stack) {
        return variant.isValid(tank, FluidVariant.of(stack));
    }

    /**
     * Inserts an FluidStack and returns the amount that was inserted.
     * Distribution of the fluid across the tanks is left to the storage implementation.
     * The FluidStack will not be modified in this function!
     *
     * <p>This function is similar to the deprecated {@code IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}.
     *
     * @param stack    FluidStack to insert.
     * @param simulate If true, the insertion is only simulated
     * @return Amount of resource that was (or would have been, if simulated) filled.
     */
    public static int fill(Storage<FluidVariant> storage, FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return 0;
        }
        try (var tx = Transaction.open(null)) {
            var variant = FluidVariant.of(stack);
            int result = Ints.saturatedCast(storage.insert(variant, stack.getAmount(), tx));
            if (!simulate) {
                tx.commit();
            }
            return result;
        }
    }

    /**
     * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     *
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
     * @param simulate If true, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     * simulated) drained.
     */
    public static FluidStack drain(Storage<FluidVariant> storage, FluidStack resource, boolean simulate) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        try (var tx = Transaction.open(null)) {
            var variant = FluidVariant.of(resource);
            int result = Ints.saturatedCast(storage.extract(variant, resource.getAmount(), tx));
            if (!simulate) {
                tx.commit();
            }
            return variant.toStack(result);
        }
    }

    /**
     * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     * <p>
     * This method is not Fluid-sensitive.
     *
     * @param maxDrain Maximum amount of fluid to drain.
     * @param simulate If true, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     * simulated) drained.
     */
    public static FluidStack drain(Storage<FluidVariant> storage, int maxDrain, boolean simulate) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        try (var tx = Transaction.open(null)) {
            var extracted = StorageUtil.extractAny(storage, maxDrain, tx);
            if (extracted == null) {
                return FluidStack.EMPTY;
            }
            if (extracted.amount() > maxDrain) {
                throw new IllegalStateException("Extracted more (" + extracted
                        + ") from storage (" + storage + ") than requested (" + maxDrain + ").");
            }
            if (simulate) {
                tx.commit();
            }
            return extracted.resource().toStack((int) extracted.amount());
        }
    }
}
