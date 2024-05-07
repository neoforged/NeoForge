/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.bus.api.Event;

/**
 * Base class of the two render frame events.
 * <p>
 * These events can be used to setup and teardown global render state that must persist for the current frame.
 * <p>
 * For the event that fires once per client tick (instead of per frame), see {@link ClientTickEvent}.
 * 
 * @see RenderFrameEvent.Pre
 * @see RenderFrameEvent.Post
 */
public abstract class RenderFrameEvent extends Event {
    protected final float partialTick;

    protected RenderFrameEvent(float partialTick) {
        this.partialTick = partialTick;
    }

    /**
     * {@return the current partial tick, which is either the true partial tick or the pause partial tick, depending on if the game is paused}
     */
    public float getPartialTick() {
        return this.partialTick;
    }

    /**
     * {@link RenderFrameEvent.Pre} is fired once per frame, before the current frame is rendered via {@link GameRenderer#render(float, long, boolean)}.
     * <p>
     * This event only fires on the physical client.
     */
    public static class Pre extends RenderFrameEvent {
        public Pre(float partialTick) {
            super(partialTick);
        }
    }

    /**
     * {@link RenderFrameEvent.Post} is fired once per frame, after the current frame is rendered via {@link GameRenderer#render(float, long, boolean)}.
     * <p>
     * This event only fires on the physical client.
     */
    public static class Post extends RenderFrameEvent {
        public Post(float partialTick) {
            super(partialTick);
        }
    }
}
