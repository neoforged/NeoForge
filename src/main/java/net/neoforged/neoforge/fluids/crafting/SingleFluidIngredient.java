/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Fluid ingredient that only matches the fluid of the given stack.
 * <p>
 * Unlike with ingredients, this is an explicit "type" of fluid ingredient,
 * though it may still be written without a type field, see {@link FluidIngredient#MAP_CODEC}
 */
public class SingleFluidIngredient extends FluidIngredient {
    public static final MapCodec<SingleFluidIngredient> CODEC = BuiltInRegistries.FLUID.holderByNameCodec()
            .xmap(SingleFluidIngredient::new, SingleFluidIngredient::fluid).fieldOf("fluid");

    private final Holder<Fluid> fluid;

    public SingleFluidIngredient(Holder<Fluid> fluid) {
        if (fluid.is(Fluids.EMPTY.builtInRegistryHolder())) {
            throw new IllegalStateException("SingleFluidIngredient must not be constructed with minecraft:empty, use FluidIngredient.empty() instead!");
        }
        this.fluid = fluid;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(fluid);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.of(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
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
        return fluid.value().isSame(Fluids.EMPTY);
    }

    @Override
    public int hashCode() {
        return this.fluid().value().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof SingleFluidIngredient other && other.fluid.is(this.fluid);
    }

    public Holder<Fluid> fluid() {
        return fluid;
    }
}
