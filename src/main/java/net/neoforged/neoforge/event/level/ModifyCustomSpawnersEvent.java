/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is fired on the {@linkplain net.neoforged.fml.LogicalSide#SERVER logical server} when a {@link ServerLevel} is building its {@linkplain CustomSpawner custom spawners}.
 * <p>
 * Subscribe to this event to add/remove {@linkplain CustomSpawner custom spawners} for the level.
 * <p>
 * This event is not {@link net.neoforged.bus.api.ICancellableEvent cancellable} and does not have a result.
 */
public class ModifyCustomSpawnersEvent extends Event {
    private final ServerLevel serverLevel;
    private final List<CustomSpawner> customSpawners;

    @ApiStatus.Internal
    public ModifyCustomSpawnersEvent(ServerLevel serverLevel, List<CustomSpawner> customSpawners) {
        this.serverLevel = serverLevel;
        this.customSpawners = customSpawners;
    }

    /**
     * {@return the server level this event is affecting}
     */
    public ServerLevel getLevel() {
        return this.serverLevel;
    }

    /**
     * @return the modifiable custom spawner list.
     */
    public List<CustomSpawner> getCustomSpawners() {
        return this.customSpawners;
    }

    /**
     * Adds a custom spawner to the list.
     */
    public void addCustomSpawner(CustomSpawner customSpawner) {
        this.customSpawners.add(customSpawner);
    }
}
