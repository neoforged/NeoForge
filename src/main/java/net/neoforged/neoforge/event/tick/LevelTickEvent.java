/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.tick;

import java.util.function.BooleanSupplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class of the two level tick events.
 * 
 * @see Pre
 * @see Post
 */
public abstract class LevelTickEvent extends Event {
    private final BooleanSupplier hasTime;
    private final Level level;

    protected LevelTickEvent(BooleanSupplier hasTime, Level level) {
        this.hasTime = hasTime;
        this.level = level;
    }

    /**
     * On the server, returns true if the server has enough time to perform any
     * additional tasks (usually IO related) during the current tick.
     * 
     * On the client, always returns true.
     */
    public boolean hasTime() {
        return this.hasTime.getAsBoolean();
    }

    /**
     * {@return the level being ticked}
     */
    public Level getLevel() {
        return level;
    }

    /**
     * {@link LevelTickEvent.Pre} is fired once per game tick, per level, before the level performs work for the current tick.
     * <p>
     * This event fires on both the logical client and logical server, for {@link ClientLevel} and {@link ServerLevel} respectively.
     */
    public static class Pre extends LevelTickEvent {
        @ApiStatus.Internal
        public Pre(BooleanSupplier haveTime, Level level) {
            super(haveTime, level);
        }
    }

    /**
     * {@link LevelTickEvent.Post} is fired once per game tick, per level, after the level performs work for the current tick.
     * <p>
     * This event fires on both the logical client and logical server, for {@link ClientLevel} and {@link ServerLevel} respectively.
     */
    public static class Post extends LevelTickEvent {
        @ApiStatus.Internal
        public Post(BooleanSupplier haveTime, Level level) {
            super(haveTime, level);
        }
    }
}
