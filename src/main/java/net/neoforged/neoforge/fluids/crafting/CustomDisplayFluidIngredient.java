/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * FluidIngredient that wraps another fluid ingredient to override its {@link SlotDisplay}.
 */
public final class CustomDisplayFluidIngredient extends FluidIngredient {
    public static final MapCodec<CustomDisplayFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance
                    .group(
                            FluidIngredient.CODEC.fieldOf("base").forGetter(CustomDisplayFluidIngredient::base),
                            SlotDisplay.CODEC.fieldOf("display").forGetter(CustomDisplayFluidIngredient::display))
                    .apply(instance, CustomDisplayFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CustomDisplayFluidIngredient> STREAM_CODEC = StreamCodec.composite(
            FluidIngredient.STREAM_CODEC,
            CustomDisplayFluidIngredient::base,
            SlotDisplay.STREAM_CODEC,
            CustomDisplayFluidIngredient::display,
            CustomDisplayFluidIngredient::new);

    private final FluidIngredient base;
    private final SlotDisplay display;

    public CustomDisplayFluidIngredient(FluidIngredient base, SlotDisplay display) {
        this.base = base;
        this.display = display;
    }

    public static FluidIngredient of(FluidIngredient base, SlotDisplay display) {
        return new CustomDisplayFluidIngredient(base, display);
    }

    @Override
    public boolean test(FluidStack stack) {
        return base.test(stack);
    }

    @Override
    public Stream<Holder<Fluid>> generateFluids() {
        return base.generateFluids();
    }

    @Override
    public boolean isSimple() {
        return base.isSimple();
    }

    @Override
    public FluidIngredientType<?> getType() {
        return NeoForgeMod.CUSTOM_DISPLAY_FLUID_INGREDIENT.get();
    }

    public FluidIngredient base() {
        return base;
    }

    @Override
    public SlotDisplay display() {
        return display;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CustomDisplayFluidIngredient other &&
                Objects.equals(this.base, other.base) &&
                Objects.equals(this.display, other.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, display);
    }

    @Override
    public String toString() {
        return "CustomDisplayFluidIngredient[" +
                "base=" + base + ", " +
                "display=" + display + ']';
    }
}
