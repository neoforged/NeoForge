package org.bukkit.craftbukkit.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.block.CraftBlockState;

public class BlockStateListPopulator extends DummyGeneratorAccess {
    private final LevelAccessor world;
    private final Map<BlockPos, BlockState> dataMap = new HashMap<>();
    private final Map<BlockPos, BlockEntity> entityMap = new HashMap<>();
    private final LinkedHashMap<BlockPos, CraftBlockState> list;

    public BlockStateListPopulator(LevelAccessor world) {
        this(world, new LinkedHashMap<>());
    }

    private BlockStateListPopulator(LevelAccessor world, LinkedHashMap<BlockPos, CraftBlockState> list) {
        this.world = world;
        this.list = list;
    }

    @Override
    public BlockState getBlockState(BlockPos bp) {
        BlockState blockData = dataMap.get(bp);
        return (blockData != null) ? blockData : world.getBlockState(bp);
    }

    @Override
    public FluidState getFluidState(BlockPos bp) {
        BlockState blockData = dataMap.get(bp);
        return (blockData != null) ? blockData.getFluidState() : world.getFluidState(bp);
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos blockpos) {
        // The contains is important to check for null values
        if (entityMap.containsKey(blockpos)) {
            return entityMap.get(blockpos);
        }

        return world.getBlockEntity(blockpos);
    }

    @Override
    public boolean setBlock(BlockPos position, BlockState data, int flag) {
        position = position.immutable();
        // remove first to keep insertion order
        list.remove(position);

        dataMap.put(position, data);
        if (data.hasBlockEntity()) {
            entityMap.put(position, ((EntityBlock) data.getBlock()).newBlockEntity(position, data));
        } else {
            entityMap.put(position, null);
        }

        // use 'this' to ensure that the block state is the correct TileState
        CraftBlockState state = (CraftBlockState) CraftBlock.at(this, position).getState();
        state.setFlag(flag);
        // set world handle to ensure that updated calls are done to the world and not to this populator
        state.setWorldHandle(world);
        list.put(position, state);
        return true;
    }

    @Override
    public ServerLevel getMinecraftWorld() {
        return world.getMinecraftWorld();
    }

    public void refreshTiles() {
        for (CraftBlockState state : list.values()) {
            if (state instanceof CraftBlockEntityState) {
                ((CraftBlockEntityState<?>) state).refreshSnapshot();
            }
        }
    }

    public void updateList() {
        for (org.bukkit.block.BlockState state : list.values()) {
            state.update(true);
        }
    }

    public Set<BlockPos> getBlocks() {
        return list.keySet();
    }

    public List<CraftBlockState> getList() {
        return new ArrayList<>(list.values());
    }

    public LevelAccessor getWorld() {
        return world;
    }

    // For tree generation
    @Override
    public int getMinY() {
        return getWorld().getMinY();
    }

    @Override
    public int getHeight() {
        return getWorld().getHeight();
    }

    @Override
    public boolean isStateAtPosition(BlockPos blockpos, Predicate<BlockState> predicate) {
        return predicate.test(getBlockState(blockpos));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos bp, Predicate<FluidState> prdct) {
        return world.isFluidAtPosition(bp, prdct);
    }

    @Override
    public DimensionType dimensionType() {
        return world.dimensionType();
    }

    @Override
    public RegistryAccess registryAccess() {
        return world.registryAccess();
    }

    // Needed when a tree generates in water
    @Override
    public LevelData getLevelData() {
        return world.getLevelData();
    }

    @Override
    public long nextSubTickCount() {
        return world.nextSubTickCount();
    }

    // SPIGOT-7966: Needed for some tree generations
    @Override
    public RandomSource getRandom() {
        return world.getRandom();
    }

    // Needed for PaleMossDecorator
    @Override
    public ChunkSource getChunkSource() {
        return world.getChunkSource();
    }
}
