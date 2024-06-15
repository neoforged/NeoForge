/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.HandlerUtils;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.templates.DispenserContext;
import net.neoforged.neoforge.transfer.fluids.FluidConstants;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;

/**
 * Fills or drains a fluid container item using a Dispenser.
 */
public class DispenseFluidContainer extends DefaultDispenseItemBehavior {
    private static final DispenseFluidContainer INSTANCE = new DispenseFluidContainer();

    public static DispenseFluidContainer getInstance() {
        return INSTANCE;
    }

    private DispenseFluidContainer() {}

    private final DefaultDispenseItemBehavior dispenseBehavior = new DefaultDispenseItemBehavior();

    @Override
    public ItemStack execute(BlockSource source, ItemStack stack) {
        if (FluidUtil.getFluidContained(stack).isPresent()) {
            return dumpContainer(source, stack);
        } else {
            return fillContainer(source, stack);
        }
    }

    /**
     * Picks up fluid in front of a Dispenser and fills a container with it.
     */
    private ItemStack fillContainer(BlockSource source, ItemStack stack) {
        Level level = source.level();
        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos blockpos = source.pos().relative(dispenserFacing);

        FluidActionResult actionResult = FluidUtil.tryPickUpFluid(stack, null, level, blockpos, dispenserFacing.getOpposite());
        ItemStack resultStack = actionResult.getResult();

        if (!actionResult.isSuccess() || resultStack.isEmpty()) {
            return super.execute(source, stack);
        }

        return this.consumeWithRemainder(source, stack, resultStack);
    }

    /**
     * Drains a filled container and places the fluid in front of the Dispenser.
     */
    private ItemStack dumpContainer(BlockSource source, ItemStack stack) {
        DispenserContext context = new DispenserContext(stack);
        IResourceHandler<FluidResource> handler = context.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) {
            return super.execute(source, stack);
        }

        ResourceStack<FluidResource> resourceStack = HandlerUtils.extractAny(handler, FluidConstants.BUCKET, TransferAction.EXECUTE);
        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos blockpos = source.pos().relative(dispenserFacing);
        FluidActionResult result = FluidUtil.tryPlaceFluid(null, source.level(), InteractionHand.MAIN_HAND, blockpos, stack, fluidStack);

        if (result.isSuccess()) {
            ItemStack drainedStack = result.getResult();
            return this.consumeWithRemainder(source, stack, drainedStack);
        } else {
            return this.dispenseBehavior.dispense(source, stack);
        }
    }
}
