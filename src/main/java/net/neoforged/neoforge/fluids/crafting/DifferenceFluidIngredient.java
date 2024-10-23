/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Fluid ingredient that matches the difference of two provided fluid ingredients, i.e.
 * anything contained in {@code base} that is not in {@code subtracted}.
 *
 * @see DifferenceIngredient DifferenceIngredient, its item equivalent
 */
public final class DifferenceFluidIngredient extends FluidIngredient {
    public static final MapCodec<DifferenceFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            FluidIngredient.CODEC_NON_EMPTY.fieldOf("base").forGetter(DifferenceFluidIngredient::base),
                            FluidIngredient.CODEC_NON_EMPTY.fieldOf("subtracted").forGetter(DifferenceFluidIngredient::subtracted))
                    .apply(builder, DifferenceFluidIngredient::new));
    private final FluidIngredient base;
    private final FluidIngredient subtracted;

    public DifferenceFluidIngredient(FluidIngredient base, FluidIngredient subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    @Override
    public Stream<FluidStack> generateStacks() {
        return base.generateStacks().filter(subtracted.negate());
    }

    @Override
    public boolean test(FluidStack stack) {
        return base.test(stack) && !subtracted.test(stack);
    }

    @Override
    public boolean isSimple() {
        return base.isSimple() && subtracted.isSimple();
    }

    @Override
    public FluidIngredientType<?> getType() {
        return NeoForgeMod.DIFFERENCE_FLUID_INGREDIENT_TYPE.get();
    }

    /**
     * Gets the difference of the two fluid ingredients
     *
     * @param base       Fluid ingredient that must be matched
     * @param subtracted Fluid ingredient that must not be matched
     * @return A fluid ingredient that matches anything contained in {@code base} that is not in {@code subtracted}
     */
    public static FluidIngredient of(FluidIngredient base, FluidIngredient subtracted) {
        return new DifferenceFluidIngredient(base, subtracted);
    }

    public FluidIngredient base() {
        return base;
    }

    public FluidIngredient subtracted() {
        return subtracted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.base, this.subtracted);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof DifferenceFluidIngredient other &&
                other.base.equals(this.base) && other.subtracted.equals(this.subtracted);
    }
}
