/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.tick;

import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class of the two server tick events.
 * 
 * @see Pre
 * @see Post
 */
public abstract class ServerTickEvent extends Event {
    private final BooleanSupplier hasTime;
    private final MinecraftServer server;

    protected ServerTickEvent(BooleanSupplier hasTime, MinecraftServer server) {
        this.hasTime = hasTime;
        this.server = server;
    }

    /**
     * {@return true if the server has enough time to perform any
     * additional tasks (usually IO related) during the current tick,
     * otherwise false}
     */
    public boolean hasTime() {
        return this.hasTime.getAsBoolean();
    }

    /**
     * {@return the server instance}
     */
    public MinecraftServer getServer() {
        return server;
    }

    /**
     * {@link ServerTickEvent.Pre} is fired once per server tick, before the server performs work for the current tick.
     * <p>
     * This event only fires on the logical server.
     */
    public static class Pre extends ServerTickEvent {
        @ApiStatus.Internal
        public Pre(BooleanSupplier haveTime, MinecraftServer server) {
            super(haveTime, server);
        }
    }

    /**
     * {@link ServerTickEvent.Post} is fired once per server tick, after the server performs work for the current tick.
     * <p>
     * This event only fires on the logical server.
     */
    public static class Post extends ServerTickEvent {
        @ApiStatus.Internal
        public Post(BooleanSupplier haveTime, MinecraftServer server) {
            super(haveTime, server);
        }
    }
}
