/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.lang.ref.WeakReference;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a captured snapshot of a block, including the level, position, state, BE data, and setBlock flags.
 * <p>
 * Used to record the prior state and unwind changes if the change was denied, such as during {@link BlockEvent.BreakEvent}.
 */
public class BlockSnapshot {
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("neoforge.debugBlockSnapshot", "false"));
    private static final Logger LOGGER = LogManager.getLogger();

    private final ResourceKey<Level> dim;
    private final BlockPos pos;
    private final int flags;
    private final BlockState state;
    @Nullable
    private final CompoundTag nbt;

    private WeakReference<LevelAccessor> level;
    @Nullable
    private String toString = null;

    private BlockSnapshot(ResourceKey<Level> dim, LevelAccessor level, BlockPos pos, BlockState state, @Nullable CompoundTag nbt, int flags) {
        this.dim = dim;
        this.pos = pos.immutable();
        this.state = state;
        this.flags = flags;
        this.nbt = nbt;

        this.level = new WeakReference<>(level);

        if (DEBUG) {
            LOGGER.debug("Created " + this.toString());
        }
    }

    /**
     * Creates a new snapshot of the data at the given position.
     * 
     * @param dim   The dimension of the changed block
     * @param level The level of the changed block
     * @param pos   The position of the changed block
     * @param flag  The {@link Level#setBlock(BlockPos, BlockState, int)} flags that the block was changed with.
     * @return A captured block snapshot, containing the state and BE data from the given position.
     */
    public static BlockSnapshot create(ResourceKey<Level> dim, LevelAccessor level, BlockPos pos, int flag) {
        return new BlockSnapshot(dim, level, pos, level.getBlockState(pos), getBlockEntityTag(level, pos), flag);
    }

    /**
     * Creates a new snapshot with the default block flags ({@link Block#UPDATE_NEIGHBORS and Block#UPDATE_CLIENTS}.
     * 
     * @see #create(ResourceKey, LevelAccessor, BlockPos, int)
     */
    public static BlockSnapshot create(ResourceKey<Level> dim, LevelAccessor level, BlockPos pos) {
        return create(dim, level, pos, 3);
    }

    /**
     * {@return the recorded dimension key}
     */
    public ResourceKey<Level> getDimension() {
        return this.dim;
    }

    /**
     * {@return the recorded position}
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     * @return the recorded {@link Level#setBlock(BlockPos, BlockState, int)} flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * {@return the recorded block entity NBT data, if one was present}
     */
    @Nullable
    public CompoundTag getTag() {
        return nbt;
    }

    /**
     * {@return the snapshot's recorded block state}
     */
    public BlockState getState() {
        return this.state;
    }

    /**
     * {@return the stored level, attempting to resolve it from the current server if it has gone out of scope}
     */
    @Nullable
    public LevelAccessor getLevel() {
        LevelAccessor level = this.level.get();
        if (level == null) {
            level = ServerLifecycleHooks.getCurrentServer().getLevel(this.dim);
            this.level = new WeakReference<LevelAccessor>(level);
        }
        return level;
    }

    /**
     * {@return the current (live) block state at the recorded position, not the snapshot's recorded state}
     */
    public BlockState getCurrentState() {
        LevelAccessor level = this.getLevel();
        return level == null ? Blocks.AIR.defaultBlockState() : level.getBlockState(this.pos);
    }

    /**
     * Recreates a block entity from the stored data (pos/state/NBT) of this block snapshot.
     * 
     * @return The newly created block entity, or null if no NBT data was present, or it was invalid.
     */
    @Nullable
    public BlockEntity recreateBlockEntity(HolderLookup.Provider provider) {
        return getTag() != null ? BlockEntity.loadStatic(getPos(), getState(), getTag(), provider) : null;
    }

    /**
     * Restores this block snapshot to the target level and position with the specified flags.
     * 
     * @return true if the block was successfully updated, false otherwise.
     */
    public boolean restoreToLocation(LevelAccessor level, BlockPos pos, int flags) {
        BlockState replaced = getState();

        if (!level.setBlock(pos, replaced, flags)) {
            return false;
        }

        if (level instanceof Level realLevel) {
            BlockState current = getCurrentState();
            realLevel.sendBlockUpdated(pos, current, replaced, flags);
        }

        restoreBlockEntity(level, pos);

        if (DEBUG) {
            LOGGER.debug("Restored " + this.toString());
        }

        return true;
    }

    /**
     * Calls {@link #restoreToLocation} with the stored level, position, but custom block flags.
     */
    public boolean restore(int flags) {
        return restoreToLocation(getLevel(), getPos(), flags);
    }

    /**
     * Calls {@link #restoreToLocation} with the stored level, position, and block flags.
     */
    public boolean restore() {
        return restore(this.getFlags());
    }

    /**
     * Loads the stored {@link BlockEntity} data if one exists at the given position.
     * 
     * @return true if any data was loaded
     */
    public boolean restoreBlockEntity(LevelAccessor level, BlockPos pos) {
        BlockEntity be = null;
        if (getTag() != null) {
            be = level.getBlockEntity(pos);
            if (be != null) {
                be.loadWithComponents(getTag(), level.registryAccess());
                be.setChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        final BlockSnapshot other = (BlockSnapshot) obj;
        return this.dim.equals(other.dim) &&
                this.pos.equals(other.pos) &&
                this.state == other.state &&
                this.flags == other.flags &&
                Objects.equals(this.nbt, other.nbt);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.dim.hashCode();
        hash = 73 * hash + this.pos.hashCode();
        hash = 73 * hash + this.state.hashCode();
        hash = 73 * hash + this.flags;
        hash = 73 * hash + Objects.hashCode(this.getTag());
        return hash;
    }

    @Override
    public String toString() {
        if (toString == null) {
            this.toString = "BlockSnapshot[" +
                    "Level:" + this.dim.location() + ',' +
                    "Pos: " + this.pos + ',' +
                    "State: " + this.state + ',' +
                    "Flags: " + this.flags + ',' +
                    "NBT: " + (this.nbt == null ? "null" : this.nbt.toString()) +
                    ']';
        }
        return this.toString;
    }

    /**
     * Checks for a block entity at a given position, and saves it to NBT with full metadata if it exists.
     */
    @Nullable
    private static CompoundTag getBlockEntityTag(LevelAccessor level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be == null ? null : be.saveWithFullMetadata(level.registryAccess());
    }
}
