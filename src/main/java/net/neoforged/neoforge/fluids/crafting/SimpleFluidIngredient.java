/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidTagSlotDisplay;

/**
 * Fluid ingredient that matches the fluids specified by the given {@link HolderSet}.
 * Most commonly, this will either be a list of fluids or a fluid tag.
 * <p>
 * Unlike with ingredients, this is technically an explicit "type" of fluid ingredient,
 * though in JSON, it is still written <b>without</b> a type field, see {@link FluidIngredientCodecs#codec()}
 */
public class SimpleFluidIngredient extends FluidIngredient {
    private static final Codec<HolderSet<Fluid>> HOLDER_SET_NO_EMPTY_FLUID = HolderSetCodec.create(
            Registries.FLUID, FluidStack.FLUID_NON_EMPTY_CODEC, false);

    static final Codec<SimpleFluidIngredient> CODEC = ExtraCodecs.nonEmptyHolderSet(HOLDER_SET_NO_EMPTY_FLUID)
            .xmap(SimpleFluidIngredient::new, SimpleFluidIngredient::fluidSet);

    static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidIngredient> CONTENTS_STREAM_CODEC = ByteBufCodecs.holderSet(Registries.FLUID)
            .map(SimpleFluidIngredient::new, SimpleFluidIngredient::fluidSet);

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
    public SlotDisplay display() {
        return values.unwrapKey()
                .<SlotDisplay>map(FluidTagSlotDisplay::new)
                .orElseGet(super::display);
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
