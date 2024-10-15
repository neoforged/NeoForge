/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Fluid ingredient that matches the fluids specified by the given {@link HolderSet}.
 * Most commonly, this will either be a list of fluids or a fluid tag.
 * <p>
 * Unlike with ingredients, this is technically an explicit "type" of fluid ingredient,
 * though in JSON, it is still written <b>without</b> a type field, see {@link FluidIngredientCodecs#codec()}
 */
public class SimpleFluidIngredient extends FluidIngredient {
    public static final Codec<HolderSet<Fluid>> HOLDER_SET_NO_EMPTY_FLUID = HolderSetCodec.create(
            Registries.FLUID, FluidStack.FLUID_NON_EMPTY_CODEC, false);

    public static final Codec<SimpleFluidIngredient> CODEC = ExtraCodecs.nonEmptyHolderSet(HOLDER_SET_NO_EMPTY_FLUID)
            .xmap(SimpleFluidIngredient::new, SimpleFluidIngredient::fluidSet);

    // Note: This map codec explicitly always errors, since we want to force people to use HolderSet syntax!
    public static final MapCodec<SimpleFluidIngredient> MAP_CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
            return Stream.empty();
        }

        @Override
        public <T> DataResult<SimpleFluidIngredient> decode(DynamicOps<T> ops, MapLike<T> mapLike) {
            return DataResult.error(() -> "Simple fluid ingredients cannot be decoded using map syntax!");
        }

        @Override
        public <T> RecordBuilder<T> encode(SimpleFluidIngredient ingredient, DynamicOps<T> ops, RecordBuilder<T> builder) {
            return builder.withErrorsFrom(DataResult.error(() -> "Simple fluid ingredients cannot be encoded using map syntax! Please use vanilla syntax (namespaced:item or #tag) instead!"));
        }
    };

    private final HolderSet<Fluid> values;

    public SimpleFluidIngredient(HolderSet<Fluid> values) {
        values.unwrap().ifRight(list -> {
            if (list.isEmpty()) {
                throw new UnsupportedOperationException("Fluid ingredients can't be empty!");
            } else if (list.contains(Fluids.EMPTY.builtInRegistryHolder())) {
                throw new UnsupportedOperationException("Fluid ingredients can't contain the empty fluid");
            }
        });
        this.values = values;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return values.contains(fluidStack.getFluidHolder());
    }

    @Override
    protected Stream<Holder<Fluid>> generateFluids() {
        return values.stream();
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return NeoForgeMod.SIMPLE_FLUID_INGREDIENT_TYPE.get();
    }

    @Override
    public int hashCode() {
        return this.fluidSet().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof SimpleFluidIngredient other && other.fluidSet().equals(this.fluidSet());
    }

    public HolderSet<Fluid> fluidSet() {
        return values;
    }
}
