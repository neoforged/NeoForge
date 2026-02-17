/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

/// {@link Fluid} variant of {@link ItemStackTemplate}.
///
/// The main difference is that fluid templates are required to have an {@code amount}, while item templates default to {@code 1}.
///
/// Most methods in this class are adopted from {@link ItemStackTemplate}.
public record FluidStackTemplate(Holder<Fluid> fluid, int amount, DataComponentPatch components) implements FluidInstance {
    public static final MapCodec<FluidStackTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            FLUID_HOLDER_CODEC.fieldOf(FIELD_ID).forGetter(FluidStackTemplate::fluid),
            ExtraCodecs.POSITIVE_INT.fieldOf(FIELD_AMOUNT).forGetter(FluidStackTemplate::amount),
            DataComponentPatch.CODEC.optionalFieldOf(FIELD_COMPONENTS, DataComponentPatch.EMPTY).forGetter(FluidStackTemplate::components)).apply(i, FluidStackTemplate::new));

    public static final Codec<FluidStackTemplate> CODEC = Codec.withAlternative(MAP_CODEC.codec(), FLUID_HOLDER_CODEC, fluid -> new FluidStackTemplate(fluid.value(), FluidType.BUCKET_VOLUME));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> STREAM_CODEC = StreamCodec.composite(
            FLUID_HOLDER_STREAM_CODEC, FluidStackTemplate::fluid,
            ByteBufCodecs.VAR_INT, FluidStackTemplate::amount,
            DataComponentPatch.STREAM_CODEC, FluidStackTemplate::components,
            FluidStackTemplate::new);

    public FluidStackTemplate {
        if (fluid.is(Fluids.EMPTY.builtInRegistryHolder()) || amount <= 0) {
            throw new IllegalStateException("Fluid must be non-empty");
        }
    }

    public FluidStackTemplate(Holder<Fluid> fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidStackTemplate(Fluid fluid, int amount, DataComponentPatch components) {
        this(fluid.builtInRegistryHolder(), amount, components);
    }

    public FluidStackTemplate(Fluid fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public static FluidStackTemplate fromNonEmptyStack(FluidStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack must be non-empty");
        }

        return new FluidStackTemplate(stack.typeHolder(), stack.getAmount(), stack.getComponentsPatch());
    }

    public FluidStackTemplate withAmount(int amount) {
        return this.amount == amount ? this : new FluidStackTemplate(fluid, amount, components);
    }

    public FluidStack create() {
        return new FluidStack(fluid, amount, components);
    }

    public FluidStack apply(DataComponentPatch additionalPatch) {
        return apply(amount, additionalPatch);
    }

    public FluidStack apply(int amount, DataComponentPatch additionalPatch) {
        var stack = new FluidStack(fluid, amount, additionalPatch);
        stack.applyComponents(components);
        return stack;
    }

    @Override
    public Holder<Fluid> typeHolder() {
        return fluid;
    }

    @Override
    public @Nullable <T> T get(DataComponentType<? extends T> type) {
        return components.get(fluid.components(), type);
    }

    public static Codec<FluidStackTemplate> fixedAmountCodec(int amount) {
        return Codec.lazyInitialized(() -> RecordCodecBuilder.create(i -> i.group(
                FLUID_HOLDER_CODEC.fieldOf(FIELD_ID).forGetter(FluidStackTemplate::fluid),
                DataComponentPatch.CODEC.optionalFieldOf(FIELD_COMPONENTS, DataComponentPatch.EMPTY).forGetter(FluidStackTemplate::components)).apply(i, (holder, patch) -> new FluidStackTemplate(holder, amount, patch))));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> fixedAmountStreamCodec(int amount) {
        return StreamCodec.composite(
                FLUID_HOLDER_STREAM_CODEC, FluidStackTemplate::fluid,
                DataComponentPatch.STREAM_CODEC, FluidStackTemplate::components,
                (holder, patch) -> new FluidStackTemplate(holder, amount, patch));
    }
}
