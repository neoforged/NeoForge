/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * The AnvilCraftEvent is fired when the player {@link AnvilMenu#onTake picks up the result} of an anvil operation, "crafting" it.
 * It can be used to react to the player performing the crafting operation, and changing the side effects of doing so.
 * <p>
 * It cannot be used to modify the result of the anvil operation. To do that, use {@link AnvilUpdateEvent}.
 * <p>
 * This event is fired on both the logical server and logical client.
 *
 * @see {@link AnvilUpdateEvent} to modify the result of the anvil operation
 * @see {@link AnvilCraftEvent.Pre} for the first part of the event
 * @see {@link AnvilCraftEvent.Post} for the second part of the event
 */
public abstract class AnvilCraftEvent extends PlayerEvent {
    private final AnvilMenu menu;
    private final ItemStack left;
    private final ItemStack right;
    private final ItemStack output;

    public AnvilCraftEvent(AnvilMenu menu, Player player, ItemStack left, ItemStack right, ItemStack output) {
        super(player);
        this.menu = menu;
        this.output = output;
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the current {@link AnvilMenu}.
     * <p>
     * This should be the same menu as {@link Player#containerMenu}.
     */
    public AnvilMenu getMenu() {
        return menu;
    }

    /**
     * Returns a copy of the item stack that was picked up from the anvil output slot.
     */
    public ItemStack getOutput() {
        return output.copy();
    }

    /**
     * Returns a copy of the item stack that was in the left input slot (before the crafting operation).
     */
    public ItemStack getLeft() {
        return left.copy();
    }

    /**
     * Returns a copy of the item stack that was in the right input slot (before the crafting operation).
     */
    public ItemStack getRight() {
        return right.copy();
    }

    /**
     * This event is fired when the player picks up the result of the anvil operation, but before any post-processing occurs.
     * <p>
     * Normal post-processing includes removing the input items from the anvil, charging the player XP, and damaging the anvil block.
     * In addition, post processing is also responsible for firing {@link AnvilCraftEvent.Post}.
     */
    public static class Pre extends AnvilCraftEvent implements ICancellableEvent {
        public Pre(AnvilMenu menu, Player player, ItemStack left, ItemStack right, ItemStack output) {
            super(menu, player, left, right, output);
        }

        /**
         * Cancels the event, preventing any post-processing that occurs when the output item is picked up.
         * <p>
         * If you cancel this event, you must manually handle the post-processing yourself.
         */
        @Override
        public void setCanceled(boolean canceled) {
            ICancellableEvent.super.setCanceled(canceled);
        }
    }

    /**
     * This event is fired after the player picks up the result of the anvil operation, and all post-processing has occurred.
     * <p>
     * By this time, the input items have been removed from the anvil, the player has been charged XP, and the anvil block has been damaged (and potentially removed from the level).
     */
    public static class Post extends AnvilCraftEvent {
        public Post(AnvilMenu menu, Player player, ItemStack left, ItemStack right, ItemStack output) {
            super(menu, player, left, right, output);
        }
    }
}
