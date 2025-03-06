/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * ExplosionEvent triggers when an explosion happens in the level.<br>
 * <br>
 * ExplosionEvent.Start is fired before the explosion actually occurs.<br>
 * ExplosionEvent.Detonate is fired once the explosion has a list of affected blocks and entities.<br>
 * <br>
 * ExplosionEvent.Start is {@link ICancellableEvent}.<br>
 * ExplosionEvent.Detonate can modify the affected blocks and entities.<br>
 * Children do not use {@link HasResult}.<br>
 * Children of this event are fired on the {@link NeoForge#EVENT_BUS}.<br>
 */
public abstract class ExplosionEvent extends Event {
    private final Level level;
    private final ServerExplosion explosion;

    public ExplosionEvent(Level level, ServerExplosion explosion) {
        this.level = level;
        this.explosion = explosion;
    }

    public Level getLevel() {
        return level;
    }

    public ServerExplosion getExplosion() {
        return explosion;
    }

    /**
     * ExplosionEvent.Start is fired before the explosion actually occurs. Canceling this event will stop the explosion.<br>
     * <br>
     * This event is {@link ICancellableEvent}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     */
    public static class Start extends ExplosionEvent implements ICancellableEvent {
        public Start(Level level, ServerExplosion explosion) {
            super(level, explosion);
        }
    }

    /**
     * ExplosionEvent.Detonate is fired once the explosion has a list of affected blocks and entities. These lists can be modified to change the outcome.<br>
     * <br>
     * This event is not {@link ICancellableEvent}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     */
    public static class Detonate extends ExplosionEvent {
        private final List<Entity> entityList;
        private final List<BlockPos> blockList;

        public Detonate(Level level, ServerExplosion explosion, List<Entity> entityList, List<BlockPos> blockList) {
            super(level, explosion);
            this.entityList = entityList;
            this.blockList = blockList;
        }

        /** return the list of blocks affected by the explosion. */
        public List<BlockPos> getAffectedBlocks() {
            return this.blockList;
        }

        /** return the list of entities affected by the explosion. */
        public List<Entity> getAffectedEntities() {
            return entityList;
        }
    }
}
