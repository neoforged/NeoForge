/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.potion;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

// Copy of FluidBucketWrapper to allow differing capacities and empty containers
// builds contained fluid from the PotionContents component
public class PotionFluidHandlerItem implements IFluidHandlerItem {
    private ItemStack container;
    private final ItemLike emptyContainer;
    private final int capacity;

    public PotionFluidHandlerItem(ItemStack container, ItemLike emptyContainer, int capacity) {
        this.container = container;
        this.emptyContainer = emptyContainer;
        this.capacity = capacity;
    }

    public void setFluid(FluidStack stack) {
        if (stack.isEmpty())
            container = emptyContainer.asItem().getDefaultInstance();
        else
            container = FluidUtil.getFilledBucket(stack);
    }

    public FluidStack getFluid() {
        var container = getContainer();

        if (container.has(DataComponents.POTION_CONTENTS)) {
            var stack = new FluidStack(NeoForgeMod.POTION, capacity);
            stack.copyFrom(container, DataComponents.POTION_CONTENTS);
            return stack;
        }

        return FluidStack.EMPTY;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return fluid.has(DataComponents.POTION_CONTENTS);
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && canFillFluidType(stack);
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? getCapacity() : -1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 ? getFluid() : FluidStack.EMPTY;
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (getContainer().getCount() != 1 || resource.getAmount() < getCapacity() || !getFluid().isEmpty() || !canFillFluidType(resource))
            return 0;
        if (action.execute())
            setFluid(resource);

        return getCapacity();
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (getContainer().getCount() != 1 || resource.getAmount() < getCapacity())
            return FluidStack.EMPTY;

        var fluid = getFluid();

        if (!fluid.isEmpty() && FluidStack.isSameFluidSameComponents(fluid, resource)) {
            if (action.execute())
                setFluid(FluidStack.EMPTY);

            return fluid;
        }

        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (getContainer().getCount() != 1 || maxDrain < getCapacity())
            return FluidStack.EMPTY;

        var fluid = getFluid();

        if (!fluid.isEmpty()) {
            if (action.execute())
                setFluid(FluidStack.EMPTY);

            return fluid;
        }

        return FluidStack.EMPTY;
    }
}
