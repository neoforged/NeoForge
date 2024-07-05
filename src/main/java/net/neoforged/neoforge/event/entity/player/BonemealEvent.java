/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * This event is called when a player attempts to use bone meal on a block.
 * <p>
 * This event can be cancelled, preventing vanilla handling from occurring.
 * If you want to perform custom logic, cancel the event and perform your own handling.
 * Use {@link #setSuccessful(boolean)} to control if handling should believe bone meal was used.
 * <p>
 * This event is fired on both client and server.
 */
public class BonemealEvent extends Event implements ICancellableEvent {
    @Nullable
    private final Player player;
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;
    private final ItemStack stack;
    private final boolean isValidBonemealTarget;
    private boolean isSuccess = false;

    public BonemealEvent(@Nullable Player player, Level level, BlockPos pos, BlockState state, ItemStack stack) {
        this.player = player;
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.stack = stack;
        this.isValidBonemealTarget = state.getBlock() instanceof BonemealableBlock bonemealable && bonemealable.isValidBonemealTarget(level, pos, state);
    }

    /**
     * {@return the player who used the bone meal, if any}
     */
    @Nullable
    public Player getPlayer() {
        return this.player;
    }

    /**
     * {@return the level}
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * {@return the position of the bone mealed block}
     */
    public BlockPos getPos() {
        return this.pos;
    }

    /**
     * {@return the state of the bone mealed block}
     */
    public BlockState getState() {
        return this.state;
    }

    /**
     * Returns the bone meal item stack.
     * <p>
     * Changes to this stack will write-back to the consumer.
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Returns true if the block is a valid bone meal target.
     * <p>
     * This is determined by {@link BonemealableBlock#isValidBonemealTarget}.
     */
    public boolean isValidBonemealTarget() {
        return this.isValidBonemealTarget;
    }

    /**
     * Cancels the event and changes the successful state.
     * <p>
     * The state controls if handlers believe bone meal was successfully applied, and
     * controls things like hand swings.
     */
    public void setSuccessful(boolean success) {
        this.isSuccess = success;
        this.setCanceled(true);
    }

    /**
     * Returns if the event is successful. Only relevant if the event {@link #isCanceled()}.
     * 
     * @see #setSuccessful(boolean)
     */
    public boolean isSuccessful() {
        return this.isSuccess;
    }

    /**
     * Cancels the event, preventing vanilla handling from being applied.
     * 
     * @see #setSuccessful(boolean)
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }
}
