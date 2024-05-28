/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.util.UUID;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;

/**
 * Parent class of the two events that fire when a {@link Player} collides with an {@link ItemEntity}.
 * 
 * @see ItemEntityPickupEvent.Pre
 * @see ItemEntityPickupEvent.Post
 */
public abstract class ItemEntityPickupEvent extends Event {
    private final Player player;
    private final ItemEntity item;
    private final @Nullable UUID itemTarget;

    public ItemEntityPickupEvent(Player player, @Nullable UUID itemTarget, ItemEntity item) {
        this.player = player;
        this.itemTarget = itemTarget;
        this.item = item;
    }

    /**
     * {@return the player who collided with the item}
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Returns the {@link ItemEntity} that was collided with.
     * <p>
     * Changes to this item entity will impact further processing by the game and other event handlers.
     * <p>
     * Modification of the stored stack {@link ItemEntity#getItem()} is legal, but consumers of this event
     * must not call {@link ItemEntity#setItem(ItemStack)}, as it will incur undefined behavior.
     */
    public ItemEntity getItemEntity() {
        return item;
    }

    /**
     * {@return the {@link ItemEntity#target target} of the current {@linkplain #getItemEntity() ItemEntity}}
     * The target represents the id of the entity allowed to pick up the item entity. If {@code null}, anyone can pick it up.
     */
    public @Nullable UUID getItemEntityTarget() {
        return itemTarget;
    }

    /**
     * This event is fired when a player collides with an {@link ItemEntity} on the ground.
     * It can be used to determine if the item may be picked up by the player.
     * <p>
     * If the pickup is successful (either by force or by default rules) {@link ItemEntityPickupEvent.Post} will be fired.
     * <p>
     * This event is only fired on the logical server.
     */
    public static class Pre extends ItemEntityPickupEvent {
        private TriState canPickup = TriState.DEFAULT;

        public Pre(Player player, @Nullable UUID itemTarget, ItemEntity item) {
            super(player, itemTarget, item);
        }

        /**
         * Changes if the player may pickup the item. Setting {@link TriState#TRUE} or {@link TriState#FALSE} will allow/deny the pickup respectively.
         * <p>
         * The default rules require that {@link ItemEntity#pickupDelay} is zero, and that {@link ItemEntity#target} matches (or is null).
         * 
         * @param state The new pickup state.
         */
        public void setCanPickup(TriState state) {
            this.canPickup = state;
        }

        /**
         * {@return the current pickup state}
         * 
         * @see #setCanPickup(TriState)
         */
        public TriState canPickup() {
            return this.canPickup;
        }
    }

    /**
     * This event is fired when an {@link ItemEntity} on the ground has been picked up by the player
     * and after the item is added to the player's inventory.
     * <p>
     * This event only fires if part of the item was picked up by the player.
     * <p>
     * If the {@linkplain ItemEntity#getItem() remaining item stack} is empty, the item entity will be discarded.
     * <p>
     * This event is only fired on the logical server.
     */
    public static class Post extends ItemEntityPickupEvent {
        private final ItemStack originalStack;

        public Post(Player player, @Nullable UUID itemTarget, ItemEntity item, ItemStack originalStack) {
            super(player, itemTarget, item);
            this.originalStack = originalStack;
        }

        /**
         * Returns a copy of the original stack, before it was added to the player's inventory.
         * Changes to this item stack have no effect on any further processing.
         */
        public ItemStack getOriginalStack() {
            return this.originalStack.copy();
        }

        /**
         * Returns a live reference to the remaining stack held by the {@link ItemEntity}.
         */
        public ItemStack getCurrentStack() {
            return this.getItemEntity().getItem();
        }
    }
}
