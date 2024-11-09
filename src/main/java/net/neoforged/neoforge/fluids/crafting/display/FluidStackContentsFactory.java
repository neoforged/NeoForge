/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting.display;

import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Base fluid stack contents factory: directly returns the stacks.
 *
 * <p>Fluid equivalent of {@link SlotDisplay.ItemStackContentsFactory}.
 */
public class FluidStackContentsFactory implements ForFluidStacks<FluidStack> {
    public static final FluidStackContentsFactory INSTANCE = new FluidStackContentsFactory();

    @Override
    public FluidStack forStack(FluidStack fluid) {
        return fluid;
    }
}
