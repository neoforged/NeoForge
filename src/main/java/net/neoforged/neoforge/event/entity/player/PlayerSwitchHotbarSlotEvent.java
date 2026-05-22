/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when a player switches the hotbar slot either by
 * mouse scroll or keyboard input. If the server doesn't
 * receive the packet, neither of the two events will be
 * fired.
 *
 * @see PlayerSwitchHotbarSlotEvent.Pre
 * @see PlayerSwitchHotbarSlotEvent.Post
 */
public abstract sealed class PlayerSwitchHotbarSlotEvent extends PlayerEvent {
    private final int oldSlotIndex;
    private final int newSlotIndex;

    public PlayerSwitchHotbarSlotEvent(Player player, int oldSlotIndex, int newSlotIndex) {
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

    /**
     * Fired after the server receives the packet and is about to perform
     * the slot switching logic. If you cancel this event, you will be
     * expected to handle the logic yourself.
     * <p>
     * Vanilla logic is shown as follows:
     * <pre>{@code
     * if (newSlotIndex >= 0 && newSlotIndex <= Inventory.getSelectionSize()) {
     *     if (oldSlotIndex != newSlotIndex
     *      && player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
     *         player.stopUsingItem();
     *     }
     *     player.getInventory().setSelectedSlot(newSlotIndex);
     *     player.resetLastActionTime();
     * } else {
     *     // Invalid newSlotIndex, either warn in the logger, or throw an exception
     * }
     * }</pre>
     */
    public static non-sealed class Pre extends PlayerSwitchHotbarSlotEvent implements ICancellableEvent {
        public Pre(Player player, int oldSlotIndex, int newSlotIndex) {
            super(player, oldSlotIndex, newSlotIndex);
        }

        @Override
        public void setCanceled(boolean canceled) {
            ICancellableEvent.super.setCanceled(canceled);
        }
    }

    /**
     * Fired after all the slot switching logic is successfully
     * performed.
     */
    public static non-sealed class Post extends PlayerSwitchHotbarSlotEvent {
        public Post(Player player, int oldSlotIndex, int newSlotIndex) {
            super(player, oldSlotIndex, newSlotIndex);
        }
    }
}
