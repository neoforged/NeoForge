/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;

/**
 * There is no new interface as this was built to support IFluidHandler. IResourceHandler has no such data structure replacement.
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public interface IFluidTank {
    /**
     * @return FluidStack representing the fluid in the tank, null if the tank is empty.
     */
    FluidStack getFluid();

    /**
     * @return Current amount of fluid in the tank.
     */
    int getFluidAmount();

    /**
     * @return Capacity of this fluid tank.
     */
    int getCapacity();

    /**
     * @param stack Fluidstack holding the Fluid to be queried.
     * @return If the tank can hold the fluid (EVER, not at the time of query).
     */
    boolean isFluidValid(FluidStack stack);

    /**
     * @param resource FluidStack attempting to fill the tank.
     * @param action   If SIMULATE, the fill will only be simulated.
     * @return Amount of fluid that was accepted (or would be, if simulated) by the tank.
     */
    int fill(FluidStack resource, IFluidHandler.FluidAction action);

    /**
     * @param maxDrain Maximum amount of fluid to be removed from the container.
     * @param action   If SIMULATE, the drain will only be simulated.
     * @return Amount of fluid that was removed (or would be, if simulated) from the tank.
     */
    FluidStack drain(int maxDrain, IFluidHandler.FluidAction action);

    /**
     * @param resource Maximum amount of fluid to be removed from the container.
     * @param action   If SIMULATE, the drain will only be simulated.
     * @return FluidStack representing fluid that was removed (or would be, if simulated) from the tank.
     */
    FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action);
}
