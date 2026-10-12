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
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/**
 * Slot display for a given fluid stack, including fluid amount and data components.
 *
 * @param template The fluid stack to be displayed.
 */
public record FluidStackSlotDisplay(FluidStackTemplate template) implements SlotDisplay {
    public static final MapCodec<FluidStackSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(FluidStackTemplate.CODEC.fieldOf("fluid").forGetter(FluidStackSlotDisplay::template))
            .apply(instance, FluidStackSlotDisplay::new));
    /// Codec for a [FluidStackSlotDisplay] that encapsulates a [FluidStack].
    ///
    /// This remains for compatibility with older mods and older NeoForge versions. Use [#TEMPLATE_STREAM_CODEC] _only if_ both sides are guaranteed to use the same codec.
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackSlotDisplay> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC, FluidStackSlotDisplay::stack, FluidStackSlotDisplay::new);
    /// Codec for a [FluidStackSlotDisplay] that encapsulates a [FluidStackTemplate].
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackSlotDisplay> TEMPLATE_STREAM_CODEC = StreamCodec.composite(
            FluidStackTemplate.STREAM_CODEC, FluidStackSlotDisplay::template, FluidStackSlotDisplay::new);

    /// @deprecated This remains only for compatibility with older mods. Use [#FluidStackSlotDisplay(FluidStackTemplate)].
    @Deprecated
    public FluidStackSlotDisplay(FluidStack stack) {
        this(stack.isEmpty() ? new FluidStackTemplate(Fluids.EMPTY, 0) : FluidStackTemplate.fromNonEmptyStack(stack));
    }

    /// @deprecated This remains only for compatibility with older mods. Use [#template()] instead.
    @Deprecated
    public FluidStack stack() {
        return template.create();
    }

    @Override
    public SlotDisplay.Type<FluidStackSlotDisplay> type() {
        return NeoForgeMod.FLUID_STACK_SLOT_DISPLAY.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return switch (factory) {
            case ForFluidStacks<T> fluids -> Stream.of(fluids.forStack(template.create()));
            default -> Stream.empty();
        };
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof FluidStackSlotDisplay(FluidStackTemplate otherTemplate)
                    && this.template.equals(otherTemplate);
        }
    }
}
