/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.ForFluidStacks;

import java.util.stream.Stream;

/**
 * Superinterface for {@link Outgredient}s of type {@link FluidStack}.
 * Automatically resolves the display for {@link ForFluidStacks}.
 */
public interface FluidOutgredientSlotDisplay extends OutgredientSlotDisplay<FluidStack> {
    @Override
    default <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return factory instanceof ForFluidStacks<T> forStacks ? Stream.of(forStacks.forStack(outgredient().resolve())) : Stream.empty();
    }
}
