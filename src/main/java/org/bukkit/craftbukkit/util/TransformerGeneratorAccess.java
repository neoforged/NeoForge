package org.bukkit.craftbukkit.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.material.FluidState;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class TransformerGeneratorAccess extends DelegatedGeneratorAccess {

    private CraftStructureTransformer structureTransformer;

    public void setStructureTransformer(CraftStructureTransformer structureTransformer) {
        this.structureTransformer = structureTransformer;
    }

    public CraftStructureTransformer getStructureTransformer() {
        return structureTransformer;
    }

    @Override
    public boolean addFreshEntity(Entity arg0) {
        if (structureTransformer != null && !structureTransformer.transformEntity(arg0)) {
            return false;
        }
        return super.addFreshEntity(arg0);
    }

    @Override
    public boolean addFreshEntity(Entity arg0, SpawnReason arg1) {
        if (structureTransformer != null && !structureTransformer.transformEntity(arg0)) {
            return false;
        }
        return super.addFreshEntity(arg0, arg1);
    }

    @Override
    public void addFreshEntityWithPassengers(Entity arg0) {
        if (structureTransformer != null && !structureTransformer.transformEntity(arg0)) {
            return;
        }
        super.addFreshEntityWithPassengers(arg0);
    }

    @Override
    public void addFreshEntityWithPassengers(Entity arg0, SpawnReason arg1) {
        if (structureTransformer != null && !structureTransformer.transformEntity(arg0)) {
            return;
        }
        super.addFreshEntityWithPassengers(arg0, arg1);
    }

    public boolean setCraftBlock(BlockPos position, CraftBlockState craftBlockState, int i, int j) {
        if (structureTransformer != null) {
            craftBlockState = structureTransformer.transformCraftState(craftBlockState);
        }
        // This code is based on the method 'net.minecraft.world.level.levelgen.structure.StructurePiece#placeBlock'
        // It ensures that any kind of block is updated correctly upon placing it
        BlockState blockstate = craftBlockState.getHandle();
        boolean result = super.setBlock(position, blockstate, i, j);
        FluidState fluidstate = getFluidState(position);
        if (!fluidstate.isEmpty()) {
            scheduleTick(position, fluidstate.getType(), 0);
        }
        if (StructurePiece.SHAPE_CHECK_BLOCKS.contains(blockstate.getBlock())) {
            getChunk(position).markPosForPostProcessing(position);
        }
        BlockEntity tileEntity = getBlockEntity(position);
        if (tileEntity != null && craftBlockState instanceof CraftBlockEntityState<?> craftEntityState) {
            tileEntity.loadWithComponents(craftEntityState.getSnapshotInput());
        }
        return result;
    }

    public boolean setCraftBlock(BlockPos position, CraftBlockState craftBlockState, int i) {
        return setCraftBlock(position, craftBlockState, i, 512);
    }

    @Override
    public boolean setBlock(BlockPos position, BlockState blockstate, int i, int j) {
        if (structureTransformer == null || !structureTransformer.canTransformBlocks()) {
            return super.setBlock(position, blockstate, i, j);
        }
        return setCraftBlock(position, (CraftBlockState) CraftBlockStates.getBlockState(this, position, blockstate, null), i, j);
    }

    @Override
    public boolean setBlock(BlockPos position, BlockState blockstate, int i) {
        return setBlock(position, blockstate, i, 512);
    }
}
