package net.neoforged.neoforge.transfer.fluids.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.storage.ISingleResourceHandler;

public class CauldronWrapper implements ISingleResourceHandler<FluidResource> {
    private final Level level;
    private final BlockPos pos;

    public CauldronWrapper(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    private CauldronFluidContent getContent(BlockState state) {
        CauldronFluidContent content = CauldronFluidContent.getForBlock(state.getBlock());
        if (content == null) {
            throw new IllegalStateException("Unexpected error: no cauldron at location " + pos);
        }
        return content;
    }

    @Override
    public FluidResource getResource() {
        BlockState state = level.getBlockState(pos);
        return getContent(state).fluid.getDefaultResource();
    }

    @Override
    public int getAmount() {
        BlockState state = level.getBlockState(pos);
        return getContent(state).getMillibuckets(state);
    }

    @Override
    public int getLimit(FluidResource resource) {
        BlockState state = level.getBlockState(pos);
        return getContent(state).totalAmount;
    }

    @Override
    public boolean isValid(FluidResource resource) {
        return CauldronFluidContent.getForFluid(resource.getFluid()) != null;
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {

    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        BlockState state = level.getBlockState(pos);
        CauldronFluidContent contents = getContent(state);
        if (amount < contents.getMillibuckets(state) || resource.isBlank() || !resource.equals(contents.fluid.getDefaultResource())) {
            return 0;
        }

    }
}
