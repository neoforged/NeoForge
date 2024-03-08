/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.Hopper;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Event that is fired when ever a target pickups an item.
 * <p>
 * This event is fired on {@linkplain NeoForge#EVENT_BUS}, has no {@linkplain Result result} and is not {@linkplain ICancellableEvent cancellable}.
 */
public abstract class ItemPickupEvent extends ItemEvent {
    private final ItemStack stack;

    protected ItemPickupEvent(ItemEntity itemEntity, ItemStack stack) {
        super(itemEntity);

        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    public static final class ByPlayer extends ItemPickupEvent {
        private final Player target;

        public ByPlayer(ItemEntity itemEntity, Player target, ItemStack stack) {
            super(itemEntity, stack);

            this.target = target;
        }

        public Player getTarget() {
            return target;
        }
    }

    public static final class ByMob extends ItemPickupEvent {
        private final Mob target;

        public ByMob(ItemEntity itemEntity, Mob target, ItemStack stack) {
            super(itemEntity, stack);

            this.target = target;
        }

        public Mob getTarget() {
            return target;
        }
    }

    public static final class ByHopper extends ItemPickupEvent {
        private final Hopper target;

        public ByHopper(ItemEntity itemEntity, Hopper target, ItemStack stack) {
            super(itemEntity, stack);

            this.target = target;
        }

        public Hopper getTarget() {
            return target;
        }
    }
}
