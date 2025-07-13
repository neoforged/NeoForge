/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

/**
 * Represents a fluid-based recipe outgredient that represents the fluid as a tag-fallback combination.
 * {@link DefaultedFluidTagOutgredient#resolve()} resolves that combination into a concrete {@link FluidStack}.
 *
 * @param tagKey     The {@link TagKey} to use for looking up the outgredient.
 * @param fallback   The fallback to use if the tag-based lookup did not yield a conclusive outgredient.
 * @param amount     The amount to use. Corresponds to {@link FluidStack#getAmount()}.
 * @param components The data components to use. Corresponds to {@link FluidStack#getComponents()}.
 */
public record DefaultedFluidTagOutgredient(TagKey<Fluid> tagKey, Holder<Fluid> fallback, int amount, DataComponentPatch components) implements Outgredient<FluidStack> {
    public static final MapCodec<DefaultedFluidTagOutgredient> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(it -> it.tagKey),
            BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("fallback").forGetter(it -> it.fallback),
            ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(it -> it.amount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(it -> it.components)
    ).apply(inst, DefaultedFluidTagOutgredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DefaultedFluidTagOutgredient> STREAM_CODEC = StreamCodec.composite(
            TagKey.streamCodec(Registries.FLUID), it -> it.tagKey,
            FluidStack.FLUID_STREAM_CODEC, it -> it.fallback,
            ByteBufCodecs.VAR_INT, it -> it.amount,
            DataComponentPatch.STREAM_CODEC, it -> it.components,
            DefaultedFluidTagOutgredient::new);

    /**
     * Constructor overload that uses {@link DataComponentPatch#EMPTY} for the {@code components} parameter.
     *
     * @param tagKey   The {@link TagKey} to use for looking up the outgredient.
     * @param fallback The fallback to use if the tag-based lookup did not yield a conclusive outgredient.
     * @param amount   The amount to use. Corresponds to {@link FluidStack#getAmount()}.
     */
    public DefaultedFluidTagOutgredient(TagKey<Fluid> tagKey, Holder<Fluid> fallback, int amount) {
        this(tagKey, fallback, amount, DataComponentPatch.EMPTY);
    }

    @Override
    public FluidStack resolve() {
        Optional<Fluid> optional = NeoForgeEventHandler.getTagDefaultsManager().resolve(Registries.FLUID, tagKey);
        return new FluidStack(optional.<Holder<Fluid>>map(Fluid::builtInRegistryHolder).orElse(fallback), amount, components);
    }

    @Override
    public OutgredientType<? extends Outgredient<FluidStack>> type() {
        return NeoForgeMod.DEFAULTED_FLUID_TAG_OUTGREDIENT_TYPE.get();
    }

    @Override
    public SlotDisplay display() {
        return new Display(this);
    }

    public record Display(DefaultedFluidTagOutgredient outgredient) implements FluidOutgredientSlotDisplay {
        public static final MapCodec<Display> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                DefaultedFluidTagOutgredient.MAP_CODEC.fieldOf("outgredient").forGetter(Display::outgredient)
        ).apply(inst, Display::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Display> STREAM_CODEC = StreamCodec.composite(
                DefaultedFluidTagOutgredient.STREAM_CODEC, Display::outgredient,
                Display::new);

        @Override
        public Type<? extends SlotDisplay> type() {
            return NeoForgeMod.DEFAULTED_FLUID_TAG_OUTGREDIENT_SLOT_DISPLAY.get();
        }
    }
}
