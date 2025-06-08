/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Implement this interface as a capability which should handle fluids
 *
 * @deprecated Use {@link IResourceHandler} bound by type {@link FluidResource} instead.
 * 
 *             <pre>{@code
 *  IResourceHandler<FluidResource>
 * }</pre>
 */

@Deprecated(forRemoval = true, since = "1.21.6")
@ApiStatus.ScheduledForRemoval(inVersion = "1.22")
public interface IFluidHandler {
    enum FluidAction {
        EXECUTE, SIMULATE;

        public boolean execute() {
            return this == EXECUTE;
        }

        public boolean simulate() {
            return this == SIMULATE;
        }
    }

    /**
     * Returns the number of fluid storage units ("tanks") available
     *
     * @return The number of tanks available
     * @deprecated This is now {@link IResourceHandler#size()}
     */
    int getTanks();

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
     * @deprecated This is now {@link IResourceHandler#getResource(int)} & {@link IResourceHandler#getAmount(int)}
     */
    FluidStack getFluidInTank(int tank);

    /**
     * Retrieves the maximum fluid amount for a given tank.
     *
     * @param tank Tank to query.
     * @return The maximum fluid amount held by the tank.
     * @deprecated {@link IResourceHandler#getCapacity(int, IResource) IResourceHandler.getCapacity(int, FluidResource.EMPTY)}
     */
    int getTankCapacity(int tank);

    /**
     * This function is a way to determine which fluids can exist inside a given handler. General purpose tanks will
     * basically always return TRUE for this.
     *
     * @param tank  Tank to query for validity
     * @param stack Stack to test with for validity
     * @return TRUE if the tank can hold the FluidStack, not considering current state.
     *         (Basically, is a given fluid EVER allowed in this tank?) Return FALSE if the answer to that question is 'no.'
     * @deprecated This is now {@link IResourceHandler#isValid(int, IResource)}
     */
    boolean isFluidValid(int tank, FluidStack stack);

    /**
     * Fills fluid into internal tanks, distribution is left entirely to the IFluidHandler.
     *
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
     * @param action   If SIMULATE, fill will only be simulated.
     * @return Amount of resource that was (or would have been, if simulated) filled.
     * @deprecated This is now {@link IResourceHandler#insert(IResource, int, TransactionContext)}
     */
    int fill(FluidStack resource, FluidAction action);

    /**
     * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     *
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
     * @param action   If SIMULATE, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     *         simulated) drained.
     * @deprecated This is now {@link IResourceHandler#extract(IResource, int, TransactionContext)}
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
     * @deprecated This is now {@link ResourceHandlerUtil#extractFiltered}
     */
    FluidStack drain(int maxDrain, FluidAction action);
}
