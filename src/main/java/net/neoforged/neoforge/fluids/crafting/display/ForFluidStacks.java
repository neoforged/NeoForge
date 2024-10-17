/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting.display;

import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public interface ForFluidStacks<T> extends DisplayContentsFactory<T> {
    /**
     * {@return display data for the given fluid holder}
     *
     * @param fluid Fluid holder to display.
     */
    default T forStack(Holder<Fluid> fluid) {
        return this.forStack(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    /**
     * {@return display data for the given fluid}
     *
     * @param fluid Fluid to display.
     */
    default T forStack(Fluid fluid) {
        return this.forStack(fluid.builtInRegistryHolder());
    }

    /**
     * {@return display data for the given fluid stack}
     *
     * @param fluid Fluid stack to display
     */
    T forStack(FluidStack fluid);
}
