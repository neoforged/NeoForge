package net.neoforged.neoforge.transfer.fluids.wrappers;

import com.google.common.math.IntMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
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

    private void updateLevel(CauldronFluidContent newContent, int level) {
        BlockState newState = newContent.block.defaultBlockState();

        if (newContent.levelProperty != null) {
            newState = newState.setValue(newContent.levelProperty, level);
        }

        this.level.setBlockAndUpdate(pos, newState);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        BlockState state = level.getBlockState(pos);
        CauldronFluidContent currentContent = getContent(state);

        if (resource.isBlank() || amount <= 0 || !resource.equals(currentContent.fluid.getDefaultResource())) return 0;

        CauldronFluidContent insertContent = CauldronFluidContent.getForFluid(resource.getFluid());
        if (insertContent == null) {
            return 0;
        }

        // We can only insert increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(insertContent.maxLevel, insertContent.totalAmount);
        int amountIncrements = insertContent.totalAmount / d;
        int levelIncrements = insertContent.maxLevel / d;

        int currentLevel = currentContent.currentLevel(state);
        int insertedIncrements = Math.min(amount / amountIncrements, (insertContent.maxLevel - currentLevel) / levelIncrements);
        if (insertedIncrements > 0 && action.isExecuting()) {
            updateLevel(insertContent, currentLevel + insertedIncrements * levelIncrements);
        }

        return insertedIncrements * amountIncrements;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        BlockState state = level.getBlockState(pos);
        CauldronFluidContent content = getContent(state);
        if (amount < content.getMillibuckets(state) || resource.isBlank() || !resource.equals(content.fluid.getDefaultResource())) {
            return 0;
        }

        int d = IntMath.gcd(content.maxLevel, content.totalAmount);
        int amountIncrements = content.totalAmount / d;
        int levelIncrements = content.maxLevel / d;

        int currentLevel = content.currentLevel(state);
        int extractedIncrements = Math.min(amount / amountIncrements, currentLevel / levelIncrements);
        if (extractedIncrements > 0) {
            int newLevel = currentLevel - extractedIncrements * levelIncrements;
            if (newLevel == 0) {
                // Fully extract -> back to empty cauldron
                if (action.isExecuting()) {
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                }
            } else {
                // Otherwise just decrease levels
                if (action.isExecuting()) updateLevel(content, newLevel);
            }
        }

        return extractedIncrements * amountIncrements;
    }

}
