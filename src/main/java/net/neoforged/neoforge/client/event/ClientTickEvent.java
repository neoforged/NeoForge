/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;

/**
 * Base class of the two client tick events.
 * 
 * @see ClientTickEvent.Pre
 * @see ClientTickEvent.Post
 */
public abstract class ClientTickEvent extends Event {
    /**
     * {@link ClientTickEvent.Pre} is fired once per client tick, before the client performs work for the current tick.
     * <p>
     * This event only fires on the physical client.
     */
    public static class Pre extends ClientTickEvent {
        public Pre() {}
    }

    /**
     * {@link ClientTickEvent.Post} is fired once per client tick, after the client performs work for the current tick.
     * <p>
     * This event only fires on the physical client.
     */
    public static class Post extends ClientTickEvent {
        public Post() {}
    }
}
