/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.handlers.templates.resources.StackListHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;

// TODO: class javadoc needs a solid pass to reference all the common methods that should be overridden
public class FluidStackListHandler extends StackListHandler<FluidStack, FluidResource> {
    protected int capacity;

    public FluidStackListHandler(int size, int capacity) {
        super(size, FluidStack.EMPTY, FluidStack.OPTIONAL_CODEC);
        this.capacity = capacity;
    }

    public FluidStackListHandler(NonNullList<FluidStack> stacks, int capacity) {
        super(stacks, FluidStack.EMPTY, FluidStack.OPTIONAL_CODEC);
        this.capacity = capacity;
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
    protected FluidStack getStackFrom(FluidResource resource, int amount) {
        return resource.toStack(amount);
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return capacity;
    }

    @Override
    protected FluidStack copyOf(FluidStack stack) {
        return stack.copy();
    }

    @Override
    public boolean matches(FluidStack stack, FluidResource resource) {
        return resource.matches(stack);
    }
}
