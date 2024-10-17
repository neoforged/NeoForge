/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

/**
 * Fired when player gets an item from sources list below. Note that other means of giving item to players / other entities are not accounted for.<br>
 * For item pickups, Called after {@link ItemEntityPickupEvent.Pre} and before {@link ItemEntityPickupEvent.Post}.<br>
 * Listeners of this event is allowed to reduce the amount of this stack, but is not allowed increase or change data component of this stack.<br>
 * Mods that want to give items to player should call {@link EventHooks#addToInventory(Player, ItemStack, Source)}<br>
 * Mods that want entity to pick up items should call {@link EventHooks#onObtainItem(LivingEntity, ItemStack, Source)}<br>
 * <br>
 * Default Sources:
 * <li>ItemEntitySource for item entity pickup</li>
 * <li>ProjectileSource for arrow and trident</li>
 * <li>ItemContainerReturn for container return when consuming items (Not being used by NeoForge yet)</li>
 * <li>COMMAND for loot and give commands</li>
 * <li>REWARD for advancement rewards and modded</li>
 * <li>DROPS for modded ways of giving drops to player without creating item entities (Not being used by NeoForge yet)</li>
 * <br>
 * One example use case of this event is for backpack mods to redirect item pickup,
 * allowing them to also account for mod behaviors that directly add item to players,
 * such as wrench picking up machines, or ender pickaxe teleporting drops to player inventory.
 */
public class ObtainItemEvent extends Event {
    public static final Source REWARD = new SimpleSource("Reward");
    public static final Source COMMAND = new SimpleSource("Command");
    public static final Source DROPS = new SimpleSource("Drops");

    /**
     * @param entity The ItemEntity to be picked up
     */
    public static ItemEntitySource entity(ItemEntity entity) {
        return new ItemEntitySource(entity);
    }

    /**
     * @param entity The projectile entity to be picked up as item
     */
    public static ProjectileSource entity(Projectile entity) {
        return new ProjectileSource(entity);
    }

    /**
     * Triggers when an ItemStack is being consumed and its container is to be returned to the user
     * 
     * @param original The original ItemStack which the container used to hold.
     * @param toHand   Whether the container item is originally expected to be return to the hand using the item.
     *                 Usually true when the original item is the last item and is consumed.
     */
    public static ItemContainerReturn container(ItemStack original, boolean toHand) {
        return new ItemContainerReturn(original, toHand);
    }

    private final ItemStack stack;
    private final LivingEntity entity;
    private final Source source;

    public ObtainItemEvent(LivingEntity entity, ItemStack stack, Source source) {
        this.stack = stack;
        this.entity = entity;
        this.source = source;
    }

    public ItemStack getStack() {
        return stack;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public Source getSource() {
        return source;
    }

    public interface Source {

    }

    public interface EntitySource extends Source {
        Entity entity();
    }

    public record SimpleSource(String name) implements Source {

    }

    public record ItemEntitySource(ItemEntity entity) implements EntitySource {

    }

    public record ProjectileSource(Projectile entity) implements EntitySource {

    }

    /**
     * Triggers when an ItemStack is being consumed and its container is to be returned to the user
     * 
     * @param originalContent The original ItemStack which the container used to hold.
     * @param returnToHand    Whether the container item is originally expected to be return to the hand using the item.
     *                        Usually true when the original item is the last item and is consumed.
     */
    public record ItemContainerReturn(ItemStack originalContent, boolean returnToHand) implements Source {

    }
}
