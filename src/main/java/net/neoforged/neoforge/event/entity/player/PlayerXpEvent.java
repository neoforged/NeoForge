/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * PlayerXpEvent is fired whenever an event involving player experience occurs. <br>
 * If a method utilizes this {@link net.neoforged.bus.api.Event} as its parameter, the method will
 * receive every child event of this class.<br>
 * <br>
 * All children of this event are fired on the {@link NeoForge#EVENT_BUS}.
 */
public abstract class PlayerXpEvent extends PlayerEvent {

    public PlayerXpEvent(Player player) {
        super(player);
    }

    /**
     * This event is fired after the player collides with an experience orb, but before the player has been given the experience.
     * It can be cancelled, and no further processing will be done.
     */
    public static class PickupXp extends PlayerXpEvent implements ICancellableEvent {

        private final ExperienceOrb orb;

        public PickupXp(Player player, ExperienceOrb orb) {
            super(player);
            this.orb = orb;
        }

        public ExperienceOrb getOrb() {
            return orb;
        }

    }

    /**
     * This event is fired when the player's experience changes through the {@link Player#giveExperiencePoints(int)} method.
     * It can be cancelled, and no further processing will be done.
     */
    public static class XpChange extends PlayerXpEvent implements ICancellableEvent {

        private int amount;

        public XpChange(Player player, int amount) {
            super(player);
            this.amount = amount;
        }

        public int getAmount() {
            return this.amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

    }

    /**
     * This event is fired when the player's experience level changes through the {@link Player#giveExperienceLevels(int)} method.
     * It can be cancelled, and no further processing will be done.
     */
    public static class LevelChange extends PlayerXpEvent implements ICancellableEvent {

        private int levels;

        public LevelChange(Player player, int levels) {
            super(player);
            this.levels = levels;
        }

        public int getLevels() {
            return this.levels;
        }

        public void setLevels(int levels) {
            this.levels = levels;
        }

    }

}
