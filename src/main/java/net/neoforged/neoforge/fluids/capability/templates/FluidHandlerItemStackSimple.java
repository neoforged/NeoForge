/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability.templates;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;

/**
 * FluidHandlerItemStackSimple is a template capability provider for ItemStacks.
 * Data is stored in a {@link SimpleFluidContent} component.
 *
 * <p>This implementation only allows item containers to be fully filled or emptied, similar to vanilla buckets.
 *
 * @deprecated Use {@link ItemAccessFluidHandler}, with an override of {@link ItemAccessFluidHandler#update}
 *             to return an empty resource if the fluid amount is not exactly 0 or the capacity of the handler.
 */
@Deprecated(since = "1.21.9", forRemoval = true)
public class FluidHandlerItemStackSimple implements IFluidHandlerItem {
    protected final Supplier<DataComponentType<SimpleFluidContent>> componentType;
    protected ItemStack container;
    protected int capacity;

    /**
     * @param componentType The data component type to use for data storage.
     * @param container     The container itemStack, data is stored on it directly as NBT.
     * @param capacity      The maximum capacity of this fluid tank.
     */
    public FluidHandlerItemStackSimple(Supplier<DataComponentType<SimpleFluidContent>> componentType, ItemStack container, int capacity) {
        this.componentType = componentType;
        this.container = container;
        this.capacity = capacity;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    public FluidStack getFluid() {
        return container.getOrDefault(componentType, SimpleFluidContent.EMPTY).copy();
    }

    protected void setFluid(FluidStack fluid) {
        container.set(componentType, SimpleFluidContent.copyOf(fluid));
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
        container.remove(componentType);
    }

    /**
     * Destroys the container item when it's emptied.
     *
     * @deprecated Deprecated with no direct equivalent, however {@link ItemAccessFluidHandler} can serve as inspiration.
     *             Please open an issue on GitHub if you have a use for an equivalent of this class.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    public static class Consumable extends FluidHandlerItemStackSimple {
        public Consumable(Supplier<DataComponentType<SimpleFluidContent>> componentType, ItemStack container, int capacity) {
            super(componentType, container, capacity);
        }

        @Override
        protected void setContainerToEmpty() {
            super.setContainerToEmpty();
            container.shrink(1);
        }
    }

    /**
     * Swaps the container item for a different one when it's emptied.
     *
     * @deprecated Use {@link ItemAccessFluidHandler} instead, with an override of {@link ItemAccessFluidHandler#update}
     *             to return a different item resource if the fluid amount is 0.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    public static class SwapEmpty extends FluidHandlerItemStackSimple {
        protected final ItemStack emptyContainer;

        public SwapEmpty(Supplier<DataComponentType<SimpleFluidContent>> componentType, ItemStack container, ItemStack emptyContainer, int capacity) {
            super(componentType, container, capacity);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected void setContainerToEmpty() {
            super.setContainerToEmpty();
            container = emptyContainer;
        }
    }
}
