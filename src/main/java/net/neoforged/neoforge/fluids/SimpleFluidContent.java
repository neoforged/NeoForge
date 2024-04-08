/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stock data component class to hold a {@link FluidStack}.
 *
 * <p>A corresponding {@link DataComponentType} must be registered to use this class.
 */
public class SimpleFluidContent {
    public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidStack.EMPTY);
    public static final Codec<SimpleFluidContent> CODEC = FluidStack.OPTIONAL_CODEC
            .xmap(SimpleFluidContent::new, content -> content.fluidStack);
    public static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidContent> STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC
            .map(SimpleFluidContent::new, content -> content.fluidStack);

    private final FluidStack fluidStack;

    private SimpleFluidContent(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    public static SimpleFluidContent copyOf(FluidStack fluidStack) {
        return fluidStack.isEmpty() ? EMPTY : new SimpleFluidContent(fluidStack.copy());
    }

    public FluidStack copy() {
        return this.fluidStack.copy();
    }

    public boolean isEmpty() {
        return this.fluidStack.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SimpleFluidContent o)) {
            return false;
        } else {
            return FluidStack.matches(this.fluidStack, o.fluidStack);
        }
    }

    @Override
    public int hashCode() {
        return this.fluidStack.getAmount() * 31 + FluidStack.hashFluidAndComponents(this.fluidStack);
    }
}
