/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.FluidUtil;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * A handler for placing and picking up fluid blocks in the world.
 * <p>
 * This is commonly used with the {@link FluidUtil#tryPickupFluidAsPlayer} or {@link FluidUtil#tryPlaceFluidAsPlayer}
 */
public class BlockFluidHandler extends SnapshotJournal<BlockFluidHandler.Snapshot> implements ISingleResourceHandler<FluidResource> {
    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    protected final Player player;
    protected final Level level;
    protected final BlockPos pos;
    protected FluidStack fluid = FluidStack.EMPTY;

    public BlockFluidHandler(@Nullable Player player, Level level, BlockPos pos) {
        this.player = player;
        this.level = level;
        this.pos = pos;
    }

    public BlockFluidHandler(Level level, BlockPos pos) {
        this(null, level, pos);
    }

    @Override
    public FluidResource getResource(int index) {
        FluidState fluidState = level.getFluidState(pos);
        return fluidState.getType().getDefaultResource();
    }

    @Override
    public int getAmount(int ignoredIndex) {
        return level.getFluidState(pos).getType().getDefaultResource().isEmpty() ? 0 : FluidType.BUCKET_VOLUME;
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        //Possibly check to see if fluid resource HAS a block state
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return true;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        if (amount < FluidType.BUCKET_VOLUME) return 0;

        BlockState state = level.getBlockState(pos);

        boolean waterLoggable = state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(null, level, pos, state, resource.getInstanceValue());
        boolean replaceable = state.canBeReplaced(resource.getInstanceValue());
        FluidStack fluidstack = resource.toStack(amount);
        if ((waterLoggable || replaceable) && resource.getFluidType().isVaporizedOnPlacement(level, pos, fluidstack)) {
            updateSnapshots(transaction);
            fluid = resource.toStack(amount);
            //Handled in commit instead, though it should be noted: this likely only handles the LAST fluid inserted. Something to explore a solution to
            //resource.getFluidType().onVaporize(player, level, pos, fluidstack);
        } else if (waterLoggable) {
            updateSnapshots(transaction);
            fluid = resource.toStack(amount);
            ((LiquidBlockContainer) state.getBlock()).placeLiquid(level, pos, state, resource.getInstanceValue().defaultFluidState());
        } else if (replaceable) {
            updateSnapshots(transaction);
            fluid = resource.toStack(amount);
            FluidUtil.destroyBlockOnFluidPlacement(level, pos);
            level.setBlock(pos, resource.getInstanceValue().defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
        } else {
            return 0;
        }

        return FluidType.BUCKET_VOLUME;
    }

    protected record Snapshot(BlockState state, FluidStack fluid) {}

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        if (amount < FluidType.BUCKET_VOLUME || resource.isEmpty() || !resource.equals(fluidState.getType().getDefaultResource()))
            return 0;

        if (state.getFluidState().isEmpty()) return 0;

        if (!(state.getBlock() instanceof BucketPickup pickupHandler)) return 0;

        updateSnapshots(transaction);

        ItemStack stack = pickupHandler.pickupBlock(player, level, pos, state);
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof BucketItem bucket && !resource.equals(bucket.content.getDefaultResource())) {
                LOGGER.error("Fluid removed without successfully being picked up. Fluid {} at {} in {} matched requested type, but after performing pickup was {}.",
                        BuiltInRegistries.FLUID.getKey(fluidState.getType()), pos, level.dimension().location(), BuiltInRegistries.FLUID.getKey(bucket.content));
                return 0;
            }
            return FluidType.BUCKET_VOLUME;
        }
        return 0;
    }

    @Override
    protected BlockFluidHandler.Snapshot createSnapshot() {
        return new Snapshot(level.getBlockState(pos), fluid);
    }

    @Override
    protected void revertToSnapshot(BlockFluidHandler.Snapshot snapshot) {
        level.setBlock(pos, snapshot.state, 0);
        fluid = FluidStack.EMPTY;
    }

    @Override
    protected void onCommit(BlockFluidHandler.Snapshot originalState) {
        // State as it was modified during this outermost transaction being committed.
        BlockState state = level.getBlockState(pos);

        if (originalState.state == state) return;

        // Revert back to the blockstate before any changes happened so that the next
        // call will not short-circuit due to the blockstate not really changing.
        level.setBlock(pos, originalState.state, 0);
        // Now do the change that will trigger change notifications to other blocks/neighbors/clients
        level.setBlockAndUpdate(pos, state);
        if (fluid.getFluidType().isVaporizedOnPlacement(level, pos, fluid)) {
            // Only handles the last fluid, not the full chain
            fluid.getFluidType().onVaporize(player, level, pos, fluid);
        }
    }
}
