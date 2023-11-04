/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired from {@link PhantomSpawner#tick}, once per player, when phantoms would attempt to be spawned.<br>
 * This event is not fired for spectating players.
 * <p>
 * This event is fired before any per-player checks (but <i>after<i/> {@link Player#isSpectator()}), but after all global checks.<br>
 * The behavior of {@link PhantomSpawner} is determined by the result of this event.<br>
 * See {@link #setResult} for documentation.<br>
 * <p>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
 * 
 * @see PlayerSpawnPhantomsEvent#setResult for the effects of each result.
 */
@Event.HasResult
public class PlayerSpawnPhantomsEvent extends PlayerEvent {
    private int phantomsToSpawn;

    public PlayerSpawnPhantomsEvent(Player player, int phantomsToSpawn) {
        super(player);
        this.phantomsToSpawn = phantomsToSpawn;
    }

    /**
     * @return How many phantoms will be spawned, if spawning is successful. The default value is randomly generated.
     */
    public int getPhantomsToSpawn() {
        return phantomsToSpawn;
    }

    /**
     * Sets the number of phantoms to be spawned.
     * 
     * @param phantomsToSpawn How many phantoms should spawn, given checks are passed.
     */
    public void setPhantomsToSpawn(int phantomsToSpawn) {
        this.phantomsToSpawn = phantomsToSpawn;
    }

    /**
     * The result of this event controls if phantoms will be spawned.<br>
     * <ul>
     * <li>If the result is {@link Event.Result#ALLOW}, phantoms will always be spawned;</li>
     * <li>If the result is {@link Event.Result#DENY}, phantoms will never be spawned;</li>
     * <li>If the result is {@link Event.Result#DEFAULT}, vanilla checks will be run to determine if the spawn may occur.</li>
     * </ul>
     */
    @Override
    public void setResult(@NotNull Event.Result result) {
        super.setResult(result);
    }
}
