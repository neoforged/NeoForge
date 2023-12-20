/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event.HasResult;

/**
 * This event is fired when the game checks if players can sleep at this time.<br>
 * Failing this check will cause sleeping players to wake up and prevent awake players from sleeping.<br>
 *
 * This event has a result. {@link HasResult}<br>
 *
 * setResult(ALLOW) informs game that player can sleep at this time.<br>
 * setResult(DEFAULT) causes game to check !{@link Level#isDay()} instead.
 */
@HasResult
public class SleepingTimeCheckEvent extends PlayerEvent {
    private final Optional<BlockPos> sleepingLocation;

    public SleepingTimeCheckEvent(Player player, Optional<BlockPos> sleepingLocation) {
        super(player);
        this.sleepingLocation = sleepingLocation;
    }

    /**
     * Note that the sleeping location may be an approximated one.
     * 
     * @return The player's sleeping location.
     */
    public Optional<BlockPos> getSleepingLocation() {
        return sleepingLocation;
    }
}
