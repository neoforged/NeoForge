/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.entity;

import java.util.Optional;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Event to determine which player an XP Orb should follow.
 * <p>
 * This is fired once a second when an XP Orbit find itself without a player to move toward.
 * <p>
 * The default (nearest player within {@link #getScanDistance()}) can be overridden with
 * either a new (Fake)Player or null to cancel the attraction.
 * <p>
 * Note that providing a player that is more than 8 blocks away does work, but it will
 * cause this event to be fired again after 20 ticks.
 * <p>
 * See also: {@link PlayerXpEvent.PickupXp} for cancelling the pickup.
 * <p>
 * This event is fired on both server and client on the {@link NeoForge#EVENT_BUS}.
 */
public class XpOrbTargetingEvent extends Event {
    private final ExperienceOrb xpOrb;
    private final double scanDistance;
    @Nullable
    private Optional<Player> followingPlayer = null;

    // Not internal, modded XP Orbs may call this.
    public XpOrbTargetingEvent(ExperienceOrb xpOrb, double scanDistance) {
        this.xpOrb = xpOrb;
        this.scanDistance = scanDistance;
    }

    /**
     * The result of the event.
     */
    public @Nullable Player getFollowingPlayer() {
        return followingPlayer != null ? followingPlayer.orElse(null) : xpOrb.level().getNearestPlayer(xpOrb, scanDistance);
    }

    /**
     * Sets a new result. Can be null to cancel the default search.
     */
    public void setFollowingPlayer(@Nullable Player newFollowingPlayer) {
        this.followingPlayer = Optional.ofNullable(newFollowingPlayer);
    }

    /**
     * The {@link ExperienceOrb} that's looking for a player to follow.
     * <p>
     * You can get the {@link Level} from this.
     */
    public ExperienceOrb getXpOrb() {
        return xpOrb;
    }

    /**
     * The maximum distance to scan for players. This is 8 for vanilla orbs.
     */
    public double getScanDistance() {
        return scanDistance;
    }
}
