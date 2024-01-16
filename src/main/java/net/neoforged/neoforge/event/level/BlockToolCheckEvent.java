/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event.HasResult;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when checking if a tool can break a block.
 * Allows for force-allowing or force-denying a tool to break a block,
 * and for additional checks depending on additional context for the block (e.g. block entity context).
 * <p>
 * This event has a {@link Result}.
 * If the result is {@link Result#ALLOW}, the tool will be considered correct for breaking the block.
 * If the result is {@link Result#DENY}, the tool will not be considered correct for breaking the block.
 * If the result is {@link Result#DEFAULT}, further vanilla checks will run to determine whether the tool is considered correct for breaking the block;
 * this is vanilla behavior.
 */
@HasResult
public class BlockToolCheckEvent extends BlockEvent {
    private final ItemStack tool;
    @Nullable
    private final LivingEntity entity;

    /**
     * @param level  The level the tool check is performed in.
     * @param pos    The position the tool check is performed at.
     * @param state  The state the tool check is performed at.
     * @param entity The entity causing the tool check. May be null if it is not an entity causing the check.
     * @param tool   The tool to check.
     */
    public BlockToolCheckEvent(LevelAccessor level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack tool) {
        super(level, pos, state);
        this.entity = entity;
        this.tool = tool;
    }

    /**
     * @return The entity performing the tool check. May be null if the tool check is not caused by an entity.
     */
    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * @return The tool that will be used for the check.
     */
    public ItemStack getTool() {
        return tool;
    }
}
