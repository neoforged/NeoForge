/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a block is broken and the drops have been determined, but before they have been added to the world. This event can be used to manipulate the dropped item entities.
 * <p>
 * No guarantees can be made about the block. It will either have already been removed from the world, or will be removed after the event terminates.
 * <p>
 * If you wish to edit the state of the block in-world, use {@link BreakEvent}.
 */
public class BlockDropsEvent extends BlockEvent implements ICancellableEvent {
    @Nullable
    private final BlockEntity blockEntity;
    private final List<ItemEntity> drops;
    @Nullable
    private final Entity breaker;
    private final ItemStack tool;

    public BlockDropsEvent(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, List<ItemEntity> drops, @Nullable Entity breaker, ItemStack tool) {
        super(level, pos, state);
        this.blockEntity = blockEntity;
        this.drops = drops;
        this.breaker = breaker;
        this.tool = tool;
    }

    /**
     * Returns a mutable list of item entities that will be dropped by this block.
     * <p>
     * When this event completes successfully, all entities in this list will be added to the world.
     * 
     * @return A mutable list of item entities.
     * @apiNote Prefer using {@link LootModifier}s to add additional loot drops.
     */
    public List<ItemEntity> getDrops() {
        return this.drops;
    }

    /**
     * {@return the block entity from the current position, if available}
     */
    @Nullable
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    /**
     * {@return the entity that broke the block, or null if unknown}
     */
    @Nullable
    public Entity getBreaker() {
        return this.breaker;
    }

    /**
     * {@return the tool used when breaking this block; may be empty}
     */
    public ItemStack getTool() {
        return this.tool;
    }

    /**
     * Cancels this event, preventing any drops from being spawned and preventing {@link Block#spawnAfterBreak} from being called.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }

    @Override
    public ServerLevel getLevel() {
        return (ServerLevel) super.getLevel();
    }
}
