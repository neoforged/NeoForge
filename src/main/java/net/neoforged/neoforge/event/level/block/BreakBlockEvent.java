/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * This event is fired when a player attempts to break a block, on both the client and server.
 *
 * The following conditions may cause this event to fire in a cancelled state:
 * <ul>
 * <li>If {@link Player#blockActionRestricted} is true.</li>
 * <li>If the target block is a {@link GameMasterBlock} and {@link Player#canUseGameMasterBlocks()} is false.</li>
 * <li>If the the player is holding an item, and {@link Item#canAttackBlock} is false.</li>
 * </ul>
 *
 * In the first two cases, un-cancelling the event will not permit the block to be broken.
 * In the third case, un-cancelling will allow the break, bypassing the behavior of {@link Item#canAttackBlock}.
 */
public class BreakBlockEvent extends BlockEvent implements ICancellableEvent {
    private final Player player;
    private boolean notifyClient = false;

    public BreakBlockEvent(Level level, BlockPos pos, BlockState state, Player player) {
        super(level, pos, state);
        this.player = player;
    }

    /**
     * {@return the player who is attempting to break the block}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Canceling this event will prevent the block from being broken.
     * <p>
     * When canceled on the server, If {@link #shouldNotifyClient()}, the client will receive a {@link ClientboundBlockUpdatePacket}.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }

    /**
     * Whether or not the client should receive a {@link ClientboundBlockUpdatePacket} when this event is canceled on the server.
     */
    public boolean shouldNotifyClient() {
        return notifyClient;
    }

    /**
     * Used to enable (or disable) notifying the client.
     * <p>
     * This should be set when canceling the event only on the server, without a matching client-side event handler.
     * <p>
     * However, in general, prefer to run your event handler on both sides, rather than relying on the notifying path.
     */
    public void setNotifyClient(boolean notify) {
        this.notifyClient = notify;
    }
}
