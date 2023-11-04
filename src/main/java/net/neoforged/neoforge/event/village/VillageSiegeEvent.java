/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.village;

import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * VillageSiegeEvent is fired just before a zombie siege finds a successful location in
 * {@code VillageSiege#tryToSetupSiege(ServerLevel)}, to give mods the chance to stop the siege.<br>
 * <br>
 * This event is {@link ICancellableEvent}; canceling stops the siege.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 */
public class VillageSiegeEvent extends Event implements ICancellableEvent {
    private final VillageSiege siege;
    private final Level level;
    private final Player player;
    private final Vec3 attemptedSpawnPos;

    public VillageSiegeEvent(VillageSiege siege, Level level, Player player, Vec3 attemptedSpawnPos) {
        this.siege = siege;
        this.level = level;
        this.player = player;
        this.attemptedSpawnPos = attemptedSpawnPos;
    }

    public VillageSiege getSiege() {
        return siege;
    }

    public Level getLevel() {
        return level;
    }

    public Player getPlayer() {
        return player;
    }

    public Vec3 getAttemptedSpawnPos() {
        return attemptedSpawnPos;
    }
}
