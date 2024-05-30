package net.neoforged.neoforge.transfer.fluids.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidConstants;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.storage.templates.VoidHandler;

public class PlacementFluidHandler extends VoidHandler.Fluid {
    protected final BlockState state;
    protected final Level level;
    protected final BlockPos blockPos;

    public PlacementFluidHandler(BlockState state, Level world, BlockPos blockPos) {
        this.state = state;
        this.level = world;
        this.blockPos = blockPos;
    }

    @Override
    public int getLimit(FluidResource ignored) {
        return FluidConstants.BUCKET;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        if (amount < FluidConstants.BUCKET) return 0;
        if (action.isExecuting()) {
            if (state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(null, level, blockPos, state, resource.getFluid())) {
                container.placeLiquid(level, blockPos, state, resource.getFluid().defaultFluidState());
            } else if (state.canBeReplaced(resource.getFluid())) {
                level.destroyBlock(blockPos, true);
                level.setBlock(blockPos, state, Block.UPDATE_ALL_IMMEDIATE);
            } else {
                return 0;
            }
        }
        return FluidConstants.BUCKET;
    }
}
