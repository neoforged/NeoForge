/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.FluidUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.DispenserItemContext;
import net.neoforged.neoforge.transfer.resources.FluidResource;

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
        if (FluidUtil.getFirstFluidStackContained(stack).isEmpty()) {
            return fillContainer(source, stack);
        } else {
            return dumpContainer(source, stack);
        }
    }

    /**
     * Picks up fluid in front of a Dispenser and fills a container with it.
     */
    private ItemStack fillContainer(BlockSource source, ItemStack stack) {
        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos blockpos = source.pos().relative(dispenserFacing);

        DispenserItemContext context = new DispenserItemContext(stack);
        var handler = context.getCapability(Capabilities.FluidHandler.ITEM);

        if (handler == null || !FluidUtil.tryPickupFluid(handler, source.center(), source.level(), blockpos)) {
            return super.execute(source, stack);
        }

        return context.finalizeResult(source);
    }

    /**
     * Drains a filled container and places the fluid in front of the Dispenser.
     */
    private ItemStack dumpContainer(BlockSource source, ItemStack stack) {
        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos blockpos = source.pos().relative(dispenserFacing);

        DispenserItemContext context = new DispenserItemContext(stack);
        IResourceHandler<FluidResource> handler = context.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null || !FluidUtil.tryPlaceFluid(handler, source.center(), source.level(), blockpos)) {
            return super.execute(source, stack);
        }

        return context.finalizeResult(source);
    }
}
