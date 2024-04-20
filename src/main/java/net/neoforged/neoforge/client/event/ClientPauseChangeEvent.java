/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Fired when game pause state is about to change
 *
 * <p>These events are fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public abstract class ClientPauseChangeEvent extends Event {
    private final boolean pause;

    public ClientPauseChangeEvent(boolean pause) {
        this.pause = pause;
    }

    /**
     * Fired when {@linkplain Minecraft#pause pause} is going to change
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
     * Cancelling this event will prevent the game change pause state even if the conditions match
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Pre extends ClientPauseChangeEvent implements ICancellableEvent {
        public Pre(boolean pause) {
            super(pause);
        }
    }

    /**
     * Fired when {@linkplain Minecraft#pause pause} is already changed
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Post extends ClientPauseChangeEvent {
        public Post(boolean pause) {
            super(pause);
        }
    }

    /**
     * <p>On {@linkplain ClientPauseChangeEvent.Pre Pre} it returns what is the upcoming pause state.</p>
     * Cancelling will not modify this value.
     *
     * <p>On {@linkplain ClientPauseChangeEvent.Post Post} it returns the updated pause state</p>
     *
     * {@return the pause state}
     */
    public boolean isPaused() {
        return pause;
    }
}
