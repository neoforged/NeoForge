/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.fluids;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
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
    protected FluidStack fluidStack = FluidStack.EMPTY;

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
        Objects.checkIndex(index, size());
        FluidState fluidState = level.getFluidState(pos);
        return fluidState.getType().getDefaultResource();
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return getResource(index).isEmpty() ? 0 : FluidType.BUCKET_VOLUME;
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        //TODO Possibly check to see if fluid resource HAS a block state
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
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
        //We already are handling an insert this callback, we can't do more until closing.
        // Something like vaporizing needs to be deferred until commit, but we don't have a
        // system that allows us to queue callbacks as first-in-first-out.
        if (!fluidStack.isEmpty()) return 0;

        Fluid fluid = resource.getInstanceValue();
        FluidStack fluidstack = resource.toStack(amount);
        if (!fluid.getFluidType().canBePlacedInLevel(level, pos, fluidstack)) return 0;

        BlockState state = level.getBlockState(pos);

        boolean isFluidContainer = state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(null, level, pos, state, resource.getInstanceValue());
        boolean isReplaceable = state.canBeReplaced(resource.getInstanceValue());

        if (!isFluidContainer && !isReplaceable) return 0;
        updateSnapshots(transaction);
        fluidStack = resource.toStack(amount);

        if (resource.getFluidType().isVaporizedOnPlacement(level, pos, fluidstack)) {
            //Handled in commit instead, though it should be noted: this likely only handles the LAST fluid inserted. Something to explore a solution to
            //resource.getFluidType().onVaporize(player, level, pos, fluidstack);
        } else if (isFluidContainer) {
            //not a lot of choice during transaction. We must defer to the implementer's choice of setting block
            //TODO how should we handle this. If someone has an implementation of this class they'd like to propose, we can try it.
            ((LiquidBlockContainer) state.getBlock()).placeLiquid(level, pos, state, resource.getInstanceValue().defaultFluidState());
        } else {
            //defer breaking until commit
//            FluidUtil.destroyBlockOnFluidPlacement(level, pos);
            level.setBlock(pos, resource.getInstanceValue().defaultFluidState().createLegacyBlock(), 0);
        }

        return FluidType.BUCKET_VOLUME;
    }

    protected record Snapshot(BlockState state, FluidStack fluid) {}

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        //We already are handling an insert this callback, we can't do more until closing.
        // Something like vaporizing needs to be deferred until commit, but we don't have a
        // system that allows us to queue callbacks as first-in-first-out.
        if (!fluidStack.isEmpty()) return 0;

        BlockState state = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        if (amount < FluidType.BUCKET_VOLUME || resource.isEmpty() || !resource.equals(fluidState.getType().getDefaultResource()))
            return 0;

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
        return new Snapshot(level.getBlockState(pos), fluidStack);
    }

    @Override
    protected void revertToSnapshot(BlockFluidHandler.Snapshot snapshot) {
        level.setBlock(pos, snapshot.state, 0);
        fluidStack = FluidStack.EMPTY;
    }

    @Override
    protected void onCommit(BlockFluidHandler.Snapshot originalState) {
        // if it should be vaporized we can skip the rest of the logic
        if (fluidStack.getFluidType().isVaporizedOnPlacement(level, pos, fluidStack)) {
            fluidStack.getFluidType().onVaporize(player, level, pos, fluidStack);
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (originalState.state == state) return;

        //Set the block back to its original momentarily
        level.setBlock(pos, originalState.state, 0);
        //check to see if the original was a valid container of liquid
        boolean isValidLiquidContainer = originalState.state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(player, level, pos, originalState.state, fluidStack.getFluid());

        if (!isValidLiquidContainer) {
            //Apply the normal destruction if the block wasn't a container when placing a bucket of fluid onto the block resulting in that block to be broken.
            FluidUtil.destroyBlockOnFluidPlacement(level, pos);
        }
        //Set to our new state (that was during the commit)
        level.setBlockAndUpdate(pos, state);

        //Set back the fluidstack to empty so that the next transaction chain can begin
        fluidStack = FluidStack.EMPTY;
    }
}
