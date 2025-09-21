/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;

/**
 * Base implementation of a {@code ResourceHandler<FluidResource>} backed by a list of {@link FluidStack}s.
 *
 * <p>The following methods will typically be overridden:
 * <ul>
 * <li>(optional) {@link #isValid} to limit which resources are allowed in this handler; by default any resource is allowed.</li>
 * <li>(optional) {@link #getCapacity} to specify the capacity of this handler; by default the {@link #capacity} given in the constructor is used.</li>
 * <li>(recommended) {@link #onContentsChanged} to react to changes in this handler, for example to trigger {@code setChanged()}.</li>
 * </ul>
 */
public class FluidStacksResourceHandler extends StacksResourceHandler<FluidStack, FluidResource> {
    protected int capacity;

    public FluidStacksResourceHandler(int size, int capacity) {
        super(size, FluidStack.EMPTY, FluidStack.OPTIONAL_CODEC);
        this.capacity = capacity;
    }

    public FluidStacksResourceHandler(NonNullList<FluidStack> stacks, int capacity) {
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
