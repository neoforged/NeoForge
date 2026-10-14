/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Slot display for a given fluid stack, including fluid amount and data components.
 *
 * @param stack The fluid stack to be displayed.
 */
public record FluidStackSlotDisplay(FluidStack stack) implements SlotDisplay {
    public static final MapCodec<FluidStackSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(FluidStack.CODEC.fieldOf("fluid").forGetter(FluidStackSlotDisplay::stack))
            .apply(instance, FluidStackSlotDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackSlotDisplay> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC, FluidStackSlotDisplay::stack, FluidStackSlotDisplay::new);

    @Override
    public SlotDisplay.Type<FluidStackSlotDisplay> type() {
        return NeoForgeMod.FLUID_STACK_SLOT_DISPLAY.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return switch (factory) {
            case ForFluidStacks<T> fluids -> Stream.of(fluids.forStack(stack));
            default -> Stream.empty();
        };
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof FluidStackSlotDisplay fluidStackDisplay
                    && FluidStack.matches(this.stack, fluidStackDisplay.stack);
        }
    }
}
