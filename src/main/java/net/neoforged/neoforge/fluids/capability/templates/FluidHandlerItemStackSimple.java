/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability.templates;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidResource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceAmount;

/**
 * FluidHandlerItemStackSimple is a template capability provider for ItemStacks.
 * Data is stored in the {@link NeoForgeMod#FLUID_STACK_COMPONENT} component.
 *
 * <p>This implementation only allows item containers to be fully filled or emptied, similar to vanilla buckets.
 */
public class FluidHandlerItemStackSimple implements IFluidHandlerItem {
    protected ItemStack container;
    protected int capacity;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public FluidHandlerItemStackSimple(ItemStack container, int capacity) {
        this.container = container;
        this.capacity = capacity;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    public FluidStack getFluid() {
        var component = container.get(NeoForgeMod.FLUID_STACK_COMPONENT.get());
        return component == null ? FluidStack.EMPTY : component.resource().toStack(component.amount());
    }

    protected void setFluid(FluidStack fluid) {
        container.set(NeoForgeMod.FLUID_STACK_COMPONENT.get(), new ResourceAmount<>(FluidResource.of(fluid), fluid.getAmount()));
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }

        FluidStack contained = getFluid();
        if (contained.isEmpty()) {
            int fillAmount = Math.min(capacity, resource.getAmount());
            if (fillAmount == capacity) {
                if (action.execute()) {
                    setFluid(resource.copyWithAmount(fillAmount));
                }

                return fillAmount;
            }
        }

        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, getFluid())) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        FluidStack contained = getFluid();
        if (contained.isEmpty() || !canDrainFluidType(contained)) {
            return FluidStack.EMPTY;
        }

        final int drainAmount = Math.min(contained.getAmount(), maxDrain);
        if (drainAmount == capacity) {
            FluidStack drained = contained.copy();

            if (action.execute()) {
                setContainerToEmpty();
            }

            return drained;
        }

        return FluidStack.EMPTY;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return true;
    }

    public boolean canDrainFluidType(FluidStack fluid) {
        return true;
    }

    /**
     * Override this method for special handling.
     * Can be used to swap out the container's item for a different one with "container.setItem".
     * Can be used to destroy the container with "container.stackSize--"
     */
    protected void setContainerToEmpty() {
        container.remove(NeoForgeMod.FLUID_STACK_COMPONENT.get());
    }

    /**
     * Destroys the container item when it's emptied.
     */
    public static class Consumable extends FluidHandlerItemStackSimple {
        public Consumable(ItemStack container, int capacity) {
            super(container, capacity);
        }

        @Override
        protected void setContainerToEmpty() {
            super.setContainerToEmpty();
            container.shrink(1);
        }
    }

    /**
     * Swaps the container item for a different one when it's emptied.
     */
    public static class SwapEmpty extends FluidHandlerItemStackSimple {
        protected final ItemStack emptyContainer;

        public SwapEmpty(ItemStack container, ItemStack emptyContainer, int capacity) {
            super(container, capacity);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected void setContainerToEmpty() {
            super.setContainerToEmpty();
            container = emptyContainer;
        }
    }
}
