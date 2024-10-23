/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Slot display for a single fluid holder.
 * <p>
 * Note that information on amount and data of the displayed fluid stack depends on the provided factory!
 *
 * @param fluid The fluid to be displayed.
 */
public record FluidSlotDisplay(Holder<Fluid> fluid) implements SlotDisplay {
    public static final MapCodec<FluidSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(RegistryFixedCodec.create(Registries.FLUID).fieldOf("fluid").forGetter(FluidSlotDisplay::fluid))
            .apply(instance, FluidSlotDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSlotDisplay> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID), FluidSlotDisplay::fluid, FluidSlotDisplay::new);

    @Override
    public Type<FluidSlotDisplay> type() {
        return NeoForgeMod.FLUID_SLOT_DISPLAY.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return switch (factory) {
            case ForFluidStacks<T> fluids -> Stream.of(fluids.forStack(fluid));
            default -> Stream.empty();
        };
    }
}
