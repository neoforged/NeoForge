/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Fired by {@link Hud.HeartType#forPlayer} to allow mods to change the heart sprite which is displayed in the player's
 * health bar.
 *
 * <p>
 * This event is fired only on the client.
 */
public class PlayerHeartTypeEvent extends PlayerEvent {
    private final Hud.HeartType originalType;
    private Hud.HeartType type;

    public PlayerHeartTypeEvent(Player player, Hud.HeartType type) {
        super(player);
        this.type = type;
        this.originalType = type;
    }

    /**
     * @return The original heart type which would be displayed by vanilla.
     */
    public Hud.HeartType getOriginalType() {
        return originalType;
    }

    /**
     * @return The heart type which will be displayed on the health bar.
     */
    public Hud.HeartType getType() {
        return type;
    }

    /**
     * Set the heart sprite which will be displayed on the {@link Player}'s health bar.
     *
     * @param type The {@link Hud.HeartType} to display
     */
    public void setType(Hud.HeartType type) {
        this.type = type;
    }
}
