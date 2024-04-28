/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;

public class EmptyFluidIngredient extends FluidIngredient {
    public static final EmptyFluidIngredient INSTANCE = new EmptyFluidIngredient();

    public static final MapCodec<EmptyFluidIngredient> CODEC = MapCodec.unit(INSTANCE);

    private EmptyFluidIngredient() {}

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.isEmpty();
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.empty();
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return NeoForgeMod.EMPTY_FLUID_INGREDIENT_TYPE.get();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
