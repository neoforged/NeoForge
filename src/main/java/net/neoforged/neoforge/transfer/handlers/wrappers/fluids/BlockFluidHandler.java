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
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.FluidUtil;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * A handler for placing and picking up fluid blocks in the world.
 * <p>
 * This is commonly used with the {@link FluidUtil#tryPickupFluid} or {@link FluidUtil#tryPlaceFluid}
 */
public class BlockFluidHandler extends SnapshotJournal<BlockState> implements ISingleResourceHandler<FluidResource> {
    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    protected final Player player;
    protected final Level level;
    protected final BlockPos pos;

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
        return fluidState.getType().defaultResource();
    }

    @Override
    public int getAmount(int ignoredIndex) {
        return level.getFluidState(pos).getType().defaultResource().isEmpty() ? 0 : FluidType.BUCKET_VOLUME;
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
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isInvalidInquiry(resource, amount)) return 0;
        if (amount < FluidType.BUCKET_VOLUME) return 0;

        BlockState state = level.getBlockState(pos);

        boolean waterLoggable = state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(null, level, pos, state, resource.getInstanceValue());
        boolean replaceable = state.canBeReplaced(resource.getInstanceValue());
        if ((waterLoggable || replaceable) && resource.isVaporizedOnPlacement(level, pos)) {
            updateSnapshots(transaction);
            resource.onVaporize(player, level, pos);
        } else if (waterLoggable) {
            updateSnapshots(transaction);
            ((LiquidBlockContainer) state.getBlock()).placeLiquid(level, pos, state, resource.getInstanceValue().defaultFluidState());
        } else if (replaceable) {
            updateSnapshots(transaction);
            FluidUtil.destroyBlockOnFluidPlacement(level, pos);
            level.setBlock(pos, resource.getInstanceValue().defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
        } else {
            return 0;
        }

        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        if (amount < FluidType.BUCKET_VOLUME || resource.isEmpty() || !resource.equals(fluidState.getType().defaultResource()))
            return 0;

        if (state.getFluidState().isEmpty()) return 0;

        if (!(state.getBlock() instanceof BucketPickup pickupHandler)) return 0;

        updateSnapshots(transaction);

        ItemStack stack = pickupHandler.pickupBlock(player, level, pos, state);
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof BucketItem bucket && !resource.equals(bucket.content.defaultResource())) {
                LOGGER.error("Fluid removed without successfully being picked up. Fluid {} at {} in {} matched requested type, but after performing pickup was {}.",
                        BuiltInRegistries.FLUID.getKey(fluidState.getType()), pos, level.dimension().location(), BuiltInRegistries.FLUID.getKey(bucket.content));
                return 0;
            }
            return FluidType.BUCKET_VOLUME;
        }
        return 0;
    }

    @Override
    protected BlockState createSnapshot() {
        return level.getBlockState(pos);
    }

    @Override
    protected void revertToSnapshot(BlockState snapshot) {
        level.setBlock(pos, snapshot, 0);
    }

    @Override
    protected void onCommit(BlockState originalState) {
        // State as it was modified during this outermost transaction being committed.
        BlockState state = level.getBlockState(pos);

        if (originalState == state) return;

        // Revert back to the blockstate before any changes happened so that the next
        // call will not short-circuit due to the blockstate not really changing.
        level.setBlock(pos, originalState, 0);
        // Now do the change that will trigger change notifications to other blocks/neighbors/clients
        level.setBlockAndUpdate(pos, state);
    }
}
