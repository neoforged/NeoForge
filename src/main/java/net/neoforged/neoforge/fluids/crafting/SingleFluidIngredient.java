/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class SingleFluidIngredient extends FluidIngredient {
    public static final MapCodec<SingleFluidIngredient> CODEC = FluidStack.fixedAmountCodec(FluidType.BUCKET_VOLUME)
            .xmap(SingleFluidIngredient::new, SingleFluidIngredient::stack).fieldOf("fluid");

    private final FluidStack stack;

    public SingleFluidIngredient(FluidStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("SingleFluidIngredient should not be constructed with an empty stack, use FluidIngredient.empty() instead!");
        }
        this.stack = stack;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return FluidStack.isSameFluid(this.stack, fluidStack);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.of(stack);
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return NeoForgeMod.SINGLE_FLUID_INGREDIENT_TYPE.get();
    }

    @Override
    public boolean hasNoFluids() {
        return stack.isEmpty();
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(stack);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof SingleFluidIngredient other && FluidStack.matches(other.stack, this.stack);
    }

    public FluidStack stack() {
        return stack;
    }

    public static FluidIngredient of(FluidStack stack) {
        return stack.isEmpty() ? empty() : new SingleFluidIngredient(stack);
    }
}
