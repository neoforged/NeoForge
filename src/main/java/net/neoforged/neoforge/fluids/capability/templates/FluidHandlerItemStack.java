/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability.templates;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * FluidHandlerItemStack is a template capability provider for ItemStacks.
 * Data is stored directly in the vanilla NBT, in the same way as the old ItemFluidContainer.
 *
 * This class allows an ItemStack to contain any partial level of fluid up to its capacity, unlike {@link FluidHandlerItemStackSimple}
 *
 * Additional examples are provided to enable consumable fluid containers (see {@link Consumable}),
 * fluid containers with different empty and full items (see {@link SwapEmpty},
 */
public class FluidHandlerItemStack implements IFluidHandlerItem {
    public static final String FLUID_NBT_KEY = "Fluid";

    protected ItemStack container;
    protected int capacity;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public FluidHandlerItemStack(ItemStack container, int capacity) {
        this.container = container;
        this.capacity = capacity;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    public FluidStack getFluid() {
        CompoundTag tagCompound = container.getTag();
        if (tagCompound == null || !tagCompound.contains(FLUID_NBT_KEY)) {
            return FluidStack.EMPTY;
        }
        return FluidStack.loadFluidStackFromNBT(tagCompound.getCompound(FLUID_NBT_KEY));
    }

    protected void setFluid(FluidStack fluid) {
        if (!container.hasTag()) {
            container.setTag(new CompoundTag());
        }

        CompoundTag fluidTag = new CompoundTag();
        fluid.writeToNBT(fluidTag);
        container.getTag().put(FLUID_NBT_KEY, fluidTag);
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
    public int fill(FluidStack resource, FluidAction doFill) {
        if (container.getCount() != 1 || resource.isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }

        FluidStack contained = getFluid();
        if (contained.isEmpty()) {
            int fillAmount = Math.min(capacity, resource.getAmount());

            if (doFill.execute()) {
                setFluid(resource.copyWithAmount(fillAmount));
            }

            return fillAmount;
        } else {
            if (contained.isFluidEqual(resource)) {
                int fillAmount = Math.min(capacity - contained.getAmount(), resource.getAmount());

                if (doFill.execute() && fillAmount > 0) {
                    contained.grow(fillAmount);
                    setFluid(contained);
                }

                return fillAmount;
            }

            return 0;
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty() || !resource.isFluidEqual(getFluid())) {
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

        FluidStack drained = contained.copyWithAmount(drainAmount);

        if (action.execute()) {
            contained.shrink(drainAmount);
            if (contained.isEmpty()) {
                setContainerToEmpty();
            } else {
                setFluid(contained);
            }
        }

        return drained;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return true;
    }

    public boolean canDrainFluidType(FluidStack fluid) {
        return true;
    }

    /**
     * Override this method for special handling.
     * Can be used to swap out or destroy the container.
     */
    protected void setContainerToEmpty() {
        container.removeTagKey(FLUID_NBT_KEY);
    }

    /**
     * Destroys the container item when it's emptied.
     */
    public static class Consumable extends FluidHandlerItemStack {
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
    public static class SwapEmpty extends FluidHandlerItemStack {
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
