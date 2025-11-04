/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;

/**
 * Fills or drains a fluid container item using a Dispenser.
 */
public final class DispenseFluidContainer extends DefaultDispenseItemBehavior {
    private static final DispenseFluidContainer INSTANCE = new DispenseFluidContainer();

    public static DispenseFluidContainer getInstance() {
        return INSTANCE;
    }

    private DispenseFluidContainer() {}

    @Override
    public ItemStack execute(BlockSource source, ItemStack stack) {
        // Create an item access; for now a simple one with 1 overflow slots.
        // TODO: switch to a custom item access implementation with infinite room for dropping on overflow if needed
        var containingHandler = new ItemStacksResourceHandler(2);
        containingHandler.set(0, ItemResource.of(stack), stack.getCount());
        var itemAccess = ItemAccess.forHandlerIndex(containingHandler, 0).oneByOne();

        var resourceHandler = itemAccess.getCapability(Capabilities.Fluid.ITEM);
        if (resourceHandler == null) {
            return super.execute(source, stack);
        }

        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos targetPos = source.pos().relative(dispenserFacing);

        // First try to pick up fluid in front of the dispenser, then try to drain a filled container and place the fluid in front of the dispenser
        if (!FluidUtil.tryPickupFluid(resourceHandler, null, source.level(), targetPos, dispenserFacing.getOpposite()).isEmpty()
                || !FluidUtil.tryPlaceFluid(resourceHandler, null, source.level(), InteractionHand.MAIN_HAND, targetPos).isEmpty()) {
            var stack0 = ItemUtil.getStack(containingHandler, 0);
            var stack1 = ItemUtil.getStack(containingHandler, 1);

            // Grow by 1 to match the shrink in consumeWithRemainder
            stack0.grow(1);
            return this.consumeWithRemainder(source, stack, stack1);
        } else {
            return super.execute(source, stack);
        }
    }
}
