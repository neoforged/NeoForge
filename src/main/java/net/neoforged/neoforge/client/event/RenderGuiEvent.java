/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when the HUD is rendered to the screen.
 * See the two subclasses for listening to the two possible phases.
 *
 * @see Pre
 * @see Post
 */
public abstract class RenderGuiEvent extends Event {
    private final GuiGraphics guiGraphics;
    private final DeltaTracker partialTick;

    @ApiStatus.Internal
    protected RenderGuiEvent(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }

    public DeltaTracker getPartialTick() {
        return partialTick;
    }

    /**
     * Fired <b>before</b> the HUD is rendered to the screen.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
     * If this event is cancelled, then the overlay will not be rendered, and the corresponding {@link Post} event will
     * not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @see Post
     */
    public static class Pre extends RenderGuiEvent implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(GuiGraphics guiGraphics, DeltaTracker partialTick) {
            super(guiGraphics, partialTick);
        }
    }

    /**
     * Fired <b>after</b> the HUD is rendered to the screen, if the corresponding {@link Pre} is not cancelled.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Post extends RenderGuiEvent {
        @ApiStatus.Internal
        public Post(GuiGraphics guiGraphics, DeltaTracker partialTick) {
            super(guiGraphics, partialTick);
        }
    }
}
