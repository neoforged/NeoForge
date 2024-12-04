/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * FluidIngredient that matches if all child ingredients match
 */
public final class IntersectionFluidIngredient extends FluidIngredient {
    public IntersectionFluidIngredient(List<FluidIngredient> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an IntersectionFluidIngredient with no children, use FluidIngredient.of() to create an empty ingredient");
        }
        this.children = children;
    }

    public static final MapCodec<IntersectionFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            FluidIngredient.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("children").forGetter(IntersectionFluidIngredient::children))
                    .apply(builder, IntersectionFluidIngredient::new));
    private final List<FluidIngredient> children;

    /**
     * Gets an intersection fluid ingredient
     *
     * @param ingredients List of fluid ingredients to match
     * @return FluidIngredient that only matches if all the passed ingredients match
     */
    public static FluidIngredient of(FluidIngredient... ingredients) {
        if (ingredients.length == 0)
            throw new IllegalArgumentException("Cannot create an IntersectionFluidIngredient with no children, use FluidIngredient.of() to create an empty ingredient");
        if (ingredients.length == 1)
            return ingredients[0];

        return new IntersectionFluidIngredient(Arrays.asList(ingredients));
    }

    @Override
    public boolean test(FluidStack stack) {
        for (var child : children) {
            if (!child.test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Stream<Holder<Fluid>> generateFluids() {
        return children.stream()
                .flatMap(child -> child.fluids().stream())
                .filter(fluid -> test(new FluidStack(fluid, FluidType.BUCKET_VOLUME)));
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
        return NeoForgeMod.INTERSECTION_FLUID_INGREDIENT_TYPE.get();
    }

    public List<FluidIngredient> children() {
        return children;
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof IntersectionFluidIngredient other && children.equals(other.children);
    }
}
