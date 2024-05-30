/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability;

import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.transfer.storage.IResourceHandler;
import net.neoforged.neoforge.transfer.TransferAction;

/**
 * Implement this interface as a capability which should handle fluids, generally storing them in
 * one or more internal {@link IFluidTank} objects.
 * <p>
 * A reference implementation is provided {@link TileFluidHandler}.
 */
@Deprecated(forRemoval = true, since = "1.21")
public interface IFluidHandler extends IResourceHandler<FluidResource> {
    enum FluidAction {
        EXECUTE, SIMULATE;

        public boolean execute() {
            return this == EXECUTE;
        }

        public boolean simulate() {
            return this == SIMULATE;
        }

        public TransferAction toTransferAction() {
            return execute() ? TransferAction.EXECUTE : TransferAction.SIMULATE;
        }

        public static FluidAction fromTransferAction(TransferAction action) {
            return action.isExecuting() ? EXECUTE : SIMULATE;
        }
    }

    /**
     * Returns the number of fluid storage units ("tanks") available
     *
     * @return The number of tanks available
     */
    int getTanks();

    @Override
    default int size() {
        return getTanks();
    }

    /**
     * Returns the FluidStack in a given tank.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This FluidStack <em>MUST NOT</em> be modified. This method is not for
     * altering internal contents. Any implementers who are able to detect modification via this method
     * should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLUIDSTACK</em></strong>
     * </p>
     *
     * @param tank Tank to query.
     * @return FluidStack in a given tank. FluidStack.EMPTY if the tank is empty.
     */
    FluidStack getFluidInTank(int tank);

    @Override
    default FluidResource getResource(int index) {
        return FluidResource.of(getFluidInTank(index));
    }

    @Override
    default int getAmount(int index) {
        return getFluidInTank(index).getAmount();
    }

    /**
     * Retrieves the maximum fluid amount for a given tank.
     *
     * @param tank Tank to query.
     * @return The maximum fluid amount held by the tank.
     */
    int getTankCapacity(int tank);

    @Override
    default int getLimit(int index, FluidResource resource) {
        return getTankCapacity(index);
    }

    /**
     * This function is a way to determine which fluids can exist inside a given handler. General purpose tanks will
     * basically always return TRUE for this.
     *
     * @param tank  Tank to query for validity
     * @param stack Stack to test with for validity
     * @return TRUE if the tank can hold the FluidStack, not considering current state.
     *         (Basically, is a given fluid EVER allowed in this tank?) Return FALSE if the answer to that question is 'no.'
     */
    boolean isFluidValid(int tank, FluidStack stack);

    @Override
    default boolean isValid(int index, FluidResource resource) {
        return isFluidValid(index, resource.toStack(1));
    }

    /**
     * Fills fluid into internal tanks, distribution is left entirely to the IFluidHandler.
     *
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
     * @param action   If SIMULATE, fill will only be simulated.
     * @return Amount of resource that was (or would have been, if simulated) filled.
     */
    int fill(FluidStack resource, FluidAction action);

    /**
     * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     *
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
     * @param action   If SIMULATE, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     *         simulated) drained.
     */
    FluidStack drain(FluidStack resource, FluidAction action);

    /**
     * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     * <p>
     * This method is not Fluid-sensitive.
     *
     * @param maxDrain Maximum amount of fluid to drain.
     * @param action   If SIMULATE, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     *         simulated) drained.
     */
    FluidStack drain(int maxDrain, FluidAction action);

    @Override
    default int insert(int index, FluidResource resource, int amount, TransferAction action) {
        return insert(resource, amount, action);
    }

    @Override
    default int insert(FluidResource resource, int amount, TransferAction action) {
        return fill(resource.toStack(amount), FluidAction.fromTransferAction(action));
    }

    @Override
    default int extract(int index, FluidResource resource, int amount, TransferAction action) {
        return extract(resource, amount, action);
    }

    @Override
    default int extract(FluidResource resource, int amount, TransferAction action) {
        return drain(resource.toStack(amount), FluidAction.fromTransferAction(action)).getAmount();
    }

    @Override
    default boolean isEmpty(int slot) {
        return getFluidInTank(slot).isEmpty();
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
