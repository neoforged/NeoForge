/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.Hopper;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Event which is fired to determine whether or not item pickup should be allowed.
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable}.
 * <p>
 * This event has a {@linkplain Result result}
 * <ul>
 * <li>{@linkplain Result#ALLOW} - Forcefully allow item pickup (if possible)</li>
 * <li>{@linkplain Result#DEFAULT} - Default behavior</li>
 * <li>{@linkplain Result#DENY} - Item pickup should be disallowed</li>
 * </ul>
 *
 * This event is fired on {@linkplain NeoForge#EVENT_BUS}.
 */
@Event.HasResult
public abstract class ItemPickupAllowedEvent extends ItemEvent {
    protected ItemPickupAllowedEvent(ItemEntity itemEntity) {
        super(itemEntity);
    }

    public static final class ByPlayer extends ItemPickupAllowedEvent {
        private final Player target;

        public ByPlayer(ItemEntity itemEntity, Player target) {
            super(itemEntity);

            this.target = target;
        }

        public Player getTarget() {
            return target;
        }
    }

    public static final class ByMob extends ItemPickupAllowedEvent {
        private final Mob target;

        public ByMob(ItemEntity itemEntity, Mob target) {
            super(itemEntity);

            this.target = target;
        }

        public Mob getTarget() {
            return target;
        }
    }

    public static final class ByHopper extends ItemPickupAllowedEvent {
        private final Hopper target;

        public ByHopper(ItemEntity itemEntity, Hopper target) {
            super(itemEntity);

            this.target = target;
        }

        public Hopper getTarget() {
            return target;
        }
    }
}
