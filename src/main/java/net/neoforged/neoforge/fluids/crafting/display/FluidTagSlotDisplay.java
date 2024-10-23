/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Slot display that shows all fluids in a given tag.
 *
 * <p>Note that information on amount and data of the displayed fluid stacks depends on the provided factory!
 *
 * @param tag The tag to be displayed.
 */
public record FluidTagSlotDisplay(TagKey<Fluid> tag) implements SlotDisplay {
    public static final MapCodec<FluidTagSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_379704_ -> p_379704_.group(TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(FluidTagSlotDisplay::tag))
                    .apply(p_379704_, FluidTagSlotDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidTagSlotDisplay> STREAM_CODEC = StreamCodec.composite(
            TagKey.streamCodec(Registries.FLUID), FluidTagSlotDisplay::tag, FluidTagSlotDisplay::new);

    @Override
    public SlotDisplay.Type<FluidTagSlotDisplay> type() {
        return NeoForgeMod.FLUID_TAG_SLOT_DISPLAY.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (factory instanceof ForFluidStacks<T> fluids) {
            HolderLookup.Provider registries = context.getOptional(SlotDisplayContext.REGISTRIES);
            if (registries != null) {
                return registries.lookupOrThrow(Registries.FLUID)
                        .get(this.tag)
                        .map(p_380858_ -> p_380858_.stream().map(fluids::forStack))
                        .stream()
                        .flatMap(p_380859_ -> p_380859_);
            }
        }

        return Stream.empty();
    }
}
