/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

/**
 * Fired when player attempts to pick up an item. Note that other means of giving item to players / other entities are not accounted for.
 * Called after {@link ItemEntityPickupEvent.Pre} and before {@link ItemEntityPickupEvent.Post}.
 * Listeners of this event is allowed to reduce the amount of this stack, but is not allowed increase or change data component of this stack.
 * Mods that want to give items to player should call {@link net.neoforged.neoforge.event.EventHooks#onObtainItem(LivingEntity, ItemStack)}
 * to notify listeners of this event. <br><br>
 * One example use case of this event is for backpack mods to redirect item pickup,
 * allowing them to also account for mod behaviors that directly add item to players,
 * such as wrench picking up machines, or ender pickaxe teleporting drops to player inventory.
 */
public class ObtainItemEvent extends Event {
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

	public record ItemEntitySource(ItemEntity entity) implements Source {

	}

	public record ProjectileSource(Projectile projectile) implements Source {

	}

}
