/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;

public final class CompoundFluidIngredient extends FluidIngredient {
    public static final MapCodec<CompoundFluidIngredient> CODEC = null;

    private final List<FluidIngredient> children;

    public CompoundFluidIngredient(List<? extends FluidIngredient> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Compound fluid ingredient must have at least one child");
        }
        this.children = List.copyOf(children);
    }

    /**
     * Creates a compound ingredient from the given list of ingredients
     */
    public static FluidIngredient of(FluidIngredient... children) {
        if (children.length == 0)
            return FluidIngredient.empty();
        if (children.length == 1)
            return children[0];

        return new CompoundFluidIngredient(List.of(children));
    }

    /**
     * Creates a compound ingredient from the given list of ingredients
     */
    public static FluidIngredient of(List<FluidIngredient> children) {
        if (children.isEmpty())
            return FluidIngredient.empty();
        if (children.size() == 1)
            return children.getFirst();

        return new CompoundFluidIngredient(children);
    }

    public static FluidIngredient of(Stream<FluidIngredient> stream) {
        return of(stream.toList());
    }

    @Override
    public Stream<FluidStack> generateStacks() {
        return children.stream().flatMap(FluidIngredient::generateStacks);
    }

    @Override
    public boolean test(FluidStack stack) {
        for (var child : children) {
            if (child.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSimple() {
        for (var child : children) {
            if (!child.isSimple()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return NeoForgeMod.COMPOUND_FLUID_INGREDIENT_TYPE.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }

    @Override
    public boolean hasNoFluids() {
        return children.isEmpty() || children.stream().allMatch(FluidIngredient::hasNoFluids);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof CompoundFluidIngredient other && other.children.equals(this.children);
    }

    public List<FluidIngredient> children() {
        return children;
    }
}
