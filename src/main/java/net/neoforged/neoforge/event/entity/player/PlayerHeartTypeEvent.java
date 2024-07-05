/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;

/**
 * Fired by {@link Gui.HeartType#forPlayer} to allow mods to change the heart sprite which is displayed in the player's
 * health bar.
 *
 * <p>
 * This event is fired only on the client.
 */
public class PlayerHeartTypeEvent extends PlayerEvent {
    private final Gui.HeartType originalType;
    private Gui.HeartType type;

    public PlayerHeartTypeEvent(Player player, Gui.HeartType type) {
        super(player);
        this.type = type;
        this.originalType = type;
    }

    /**
     * @return The original heart type which would be displayed by vanilla.
     */
    public Gui.HeartType getOriginalType() {
        return originalType;
    }

    /**
     * @return The heart type which will be displayed on the health bar.
     */
    public Gui.HeartType getType() {
        return type;
    }

    /**
     * Set the heart sprite which will be displayed on the {@link Player}'s health bar.
     *
     * @param type The {@link Gui.HeartType} to display
     */
    public void setType(Gui.HeartType type) {
        this.type = type;
    }
}
