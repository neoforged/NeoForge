/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;

/// Fired when a player switches the hotbar slot either by
/// mouse scroll or keyboard input.
///
/// This event is fired on the game bus on the server side.
/// If the server doesn't receive the packet, neither of
/// the two events will be fired.
///
/// @see PlayerSwitchHotbarSlotEvent.Pre
/// @see PlayerSwitchHotbarSlotEvent.Post
public abstract class PlayerSwitchHotbarSlotEvent extends PlayerEvent {
    private final int oldSlotIndex;
    private final int newSlotIndex;

    private PlayerSwitchHotbarSlotEvent(Player player, int oldSlotIndex, int newSlotIndex) {
        super(player);
        this.oldSlotIndex = oldSlotIndex;
        this.newSlotIndex = newSlotIndex;
    }

    public int getOldSlotIndex() {
        return oldSlotIndex;
    }

    public int getNewSlotIndex() {
        return newSlotIndex;
    }

    /// Fired after the server receives the packet and is about
    /// to perform the slot switching logic. If you cancel this
    /// event, the logic will not be performed, and listeners
    /// will receive neither the pre event nor the post event.
    ///
    /// Vanilla logic is shown [here][net.minecraft.server.network.ServerGamePacketListenerImpl#handleSetCarriedItem(net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket)].
    public static class Pre extends PlayerSwitchHotbarSlotEvent implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(Player player, int oldSlotIndex, int newSlotIndex) {
            super(player, oldSlotIndex, newSlotIndex);
        }

        /// Cancels the event, preventing any further slot
        /// switching logic to be performed. Listeners will not
        /// receive this event after the cancellation.
        @Override
        public void setCanceled(boolean canceled) {
            ICancellableEvent.super.setCanceled(canceled);
        }
    }

    /// Fired after all the slot switching logic is
    /// successfully performed. This event is not cancellable.
    /// If you cancelled the pre event, this event will not
    /// be received by listeners as well.
    public static class Post extends PlayerSwitchHotbarSlotEvent {
        @ApiStatus.Internal
        public Post(Player player, int oldSlotIndex, int newSlotIndex) {
            super(player, oldSlotIndex, newSlotIndex);
        }
    }
}
