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
    default T forStack(Holder<Fluid> fluid) {
        return this.forStack(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    default T forStack(Fluid fluid) {
        return this.forStack(fluid.builtInRegistryHolder());
    }

    T forStack(FluidStack fluid);
}
