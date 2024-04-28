/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Fluid ingredient that matches the given set of fluids, additionally performing either a
 * {@link DataComponentFluidIngredient#isStrict() strict} or partial test on the FluidStack's components.
 * <p>
 * Strict ingredients will only match fluid stacks that have <b>exactly</b> the provided components, while partial ones will
 * match if the stack's components contain all required components for the {@linkplain #components input predicate}.
 * 
 * @see DataComponentIngredient DataComponentIngredient, its item equivalent
 */
public class DataComponentFluidIngredient extends FluidIngredient {
    public static final MapCodec<DataComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            HolderSetCodec.create(Registries.FLUID, BuiltInRegistries.FLUID.holderByNameCodec(), false).fieldOf("fluids").forGetter(DataComponentFluidIngredient::fluids),
                            DataComponentPredicate.CODEC.fieldOf("components").forGetter(DataComponentFluidIngredient::components),
                            Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentFluidIngredient::isStrict))
                    .apply(builder, DataComponentFluidIngredient::new));

    private final HolderSet<Fluid> fluids;
    private final DataComponentPredicate components;
    private final boolean strict;
    private final FluidStack[] stacks;

    public DataComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentPredicate components, boolean strict) {
        this.fluids = fluids;
        this.components = components;
        this.strict = strict;
        this.stacks = fluids.stream()
                .map(i -> new FluidStack(i, 1, components.asPatch()))
                .toArray(FluidStack[]::new);
    }

    @Override
    public boolean test(FluidStack stack) {
        if (strict) {
            for (FluidStack stack2 : this.stacks) {
                if (FluidStack.isSameFluidSameComponents(stack, stack2)) return true;
            }
            return false;
        } else {
            return this.fluids.contains(stack.getFluidHolder()) && this.components.test(stack);
        }
    }

    public Stream<FluidStack> generateStacks() {
        return Stream.of(stacks);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return NeoForgeMod.DATA_COMPONENT_FLUID_INGREDIENT_TYPE.get();
    }

    @Override
    public boolean hasNoFluids() {
        return stacks.length == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluids, components, strict);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DataComponentFluidIngredient other)) return false;
        return other.fluids.equals(this.fluids)
                && other.components.equals(this.components)
                && other.strict == this.strict;
    }

    public HolderSet<Fluid> fluids() {
        return fluids;
    }

    public DataComponentPredicate components() {
        return components;
    }

    public boolean isStrict() {
        return strict;
    }

    /**
     * Creates a new ingredient matching the given fluid, containing the given components
     */
    public static FluidIngredient of(boolean strict, FluidStack stack) {
        return of(strict, stack.getComponents(), stack.getFluid());
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    public static <T> FluidIngredient of(boolean strict, DataComponentType<? super T> type, T value, Fluid... fluids) {
        return of(strict, DataComponentPredicate.builder().expect(type, value).build(), fluids);
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    public static <T> FluidIngredient of(boolean strict, Supplier<? extends DataComponentType<? super T>> type, T value, Fluid... fluids) {
        return of(strict, type.get(), value, fluids);
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    public static FluidIngredient of(boolean strict, DataComponentMap map, Fluid... fluids) {
        return of(strict, DataComponentPredicate.allOf(map), fluids);
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    @SafeVarargs
    public static FluidIngredient of(boolean strict, DataComponentMap map, Holder<Fluid>... fluids) {
        return of(strict, DataComponentPredicate.allOf(map), fluids);
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    public static FluidIngredient of(boolean strict, DataComponentMap map, HolderSet<Fluid> fluids) {
        return of(strict, DataComponentPredicate.allOf(map), fluids);
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    @SafeVarargs
    public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, Holder<Fluid>... fluids) {
        return of(strict, predicate, HolderSet.direct(fluids));
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, Fluid... fluids) {
        return of(strict, predicate, HolderSet.direct(Arrays.stream(fluids).map(Fluid::builtInRegistryHolder).toList()));
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, HolderSet<Fluid> fluids) {
        return new DataComponentFluidIngredient(fluids, predicate, strict);
    }
}
