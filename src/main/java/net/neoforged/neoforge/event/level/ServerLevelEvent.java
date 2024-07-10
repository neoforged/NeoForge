/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.neoforged.neoforge.common.NeoForge;

/**
 * This event is fired whenever an event involving a {@link ServerLevel} occurs.
 * <p>
 * All children of this event are fired on the {@linkplain net.neoforged.fml.LogicalSide#SERVER logical setver} and on the {@linkplain NeoForge#EVENT_BUS game event bus}.
 */
public abstract class ServerLevelEvent extends LevelEvent {
    private final ServerLevel serverLevel;

    public ServerLevelEvent(ServerLevel serverLevel) {
        super(serverLevel);
        this.serverLevel = serverLevel;
    }

    @Override
    public ServerLevel getLevel() {
        return this.serverLevel;
    }

    /**
     * This event is fired when a {@link ServerLevel} is building its {@linkplain CustomSpawner custom spawners}.
     * <p>
     * Subscribe to this event to add/remove {@linkplain CustomSpawner custom spawners} for the level.
     * <p>
     * This event is not {@link net.neoforged.bus.api.ICancellableEvent cancellable} and does not have a result.
     */
    public static class CustomSpawners extends ServerLevelEvent {
        private final List<CustomSpawner> customSpawners;

        public CustomSpawners(ServerLevel serverLevel, List<CustomSpawner> customSpawners) {
            super(serverLevel);
            this.customSpawners = customSpawners;
        }

        /**
         * @return the modifiable custom spawner list.
         */
        public List<CustomSpawner> getCustomSpawners() {
            return this.customSpawners;
        }
    }
}
