/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a block is destroyed AFTER drops have been determined, but have not yet spawned.
 * It is safe to modify the Block in this event, as it has already been replaced.
 */
public class BlockDropsEvent extends BlockEvent implements ICancellableEvent {
    private final List<ItemEntity> dropEntities;
    private final Entity destroyingEntity;
    private final ItemStack tool;

    private boolean dropXpWhenCancelled;

    /**
     * Cancellation will result in the drops not being spawned.
     */
    public BlockDropsEvent(LevelAccessor level, BlockPos pos, BlockState state, List<ItemStack> drops, @Nullable Entity destroyer, ItemStack tool) {
        super(level, pos, state);
        this.dropEntities = new ArrayList<>();
        this.destroyingEntity = destroyer;
        this.tool = tool;
        this.dropXpWhenCancelled = true;

        for (ItemStack drop : drops) {
            Vec3 offset = pos.getCenter().add(
                    Mth.nextDouble(level.getRandom(), -0.25, 0.25),
                    Mth.nextDouble(level.getRandom(), -0.25, 0.25) - (double) EntityType.ITEM.getHeight() / 2.0,
                    Mth.nextDouble(level.getRandom(), -0.25, 0.25));
            dropEntities.add(new ItemEntity((ServerLevel) level, offset.x(), offset.y(), offset.z(), drop));
        }
    }

    /**
     * Sets whether XP Orb Entities should still be spawned when the event is cancelled.
     * 
     * @param shouldDrop Whether XP should be spawned.
     */
    public void setDropXpWhenCancelled(boolean shouldDrop) {
        this.dropXpWhenCancelled = shouldDrop;
    }

    /**
     * Returns a list of ItemEntities determined for this broken block.
     * 
     * @return A modifiable list of ItemStacks.
     */
    public List<ItemEntity> getDrops() {
        return dropEntities;
    }

    /**
     * Returns the entity associated with this broken block. Might be null.
     * 
     * @return The entity responsible for the breaking of this block, or null.
     */
    public @Nullable Entity getDestroyingEntity() {
        return destroyingEntity;
    }

    /**
     * Returns the tool associated with this broken block.
     * 
     * @return The used tool as ItemStack, or ItemStack.EMPTY
     */
    public ItemStack getTool() {
        return tool;
    }

    public boolean isDropXpWhenCancelled() {
        return dropXpWhenCancelled;
    }
}
