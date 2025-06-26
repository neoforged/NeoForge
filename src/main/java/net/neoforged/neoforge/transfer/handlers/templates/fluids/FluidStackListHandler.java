/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.handlers.templates.resource.StackListHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import org.jetbrains.annotations.Nullable;

public class FluidStackListHandler extends StackListHandler<FluidStack, FluidResource> {
    public FluidStackListHandler(int size, int capacity, @Nullable Runnable onChangedCallback) {
        super(size, FluidStack.EMPTY, capacity, FluidResource::toStack, onChangedCallback);
    }

    public FluidStackListHandler(NonNullList<FluidStack> stacks, int capacity, @Nullable Runnable onChangedCallback) {
        super(stacks, FluidStack.EMPTY, capacity, FluidResource::toStack, onChangedCallback);
    }

    @Override
    public Codec<FluidStack> stackCodec() {
        return FluidStack.OPTIONAL_CODEC;
    }

    @Override
    public FluidResource getResourceFrom(FluidStack stack) {
        return FluidResource.of(stack);
    }

    @Override
    public int getAmountFrom(FluidStack stack) {
        return stack.getAmount();
    }

    @Override
    public boolean matches(FluidStack stack, FluidResource resource) {
        return resource.is(stack);
    }

    @Override
    public FluidStack snapshotOf(FluidStack stack) {
        return stack.copy();
    }
}
