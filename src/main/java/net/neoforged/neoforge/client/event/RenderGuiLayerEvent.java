/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a GUI layer is rendered to the screen.
 * See the two subclasses for listening to the two possible phases.
 *
 * <p>A layer that is not normally active (for example because the player pressed F1) cannot be forced to render.
 * In such cases, this event will however still fire.
 *
 * @see Pre
 * @see Post
 */
public abstract class RenderGuiLayerEvent extends Event {
    private final GuiGraphics guiGraphics;
    private final float partialTick;
    private final ResourceLocation name;
    private final LayeredDraw.Layer layer;

    @ApiStatus.Internal
    protected RenderGuiLayerEvent(GuiGraphics guiGraphics, float partialTick, ResourceLocation name, LayeredDraw.Layer layer) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
        this.name = name;
        this.layer = layer;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }

    public float getPartialTick() {
        return partialTick;
    }

    public ResourceLocation getName() {
        return name;
    }

    public LayeredDraw.Layer getLayer() {
        return layer;
    }

    /**
     * Fired <b>before</b> a GUI layer is rendered to the screen.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
     * If this event is cancelled, then the layer will not be rendered, and the corresponding {@link Post} event will
     * not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @see Post
     */
    public static class Pre extends RenderGuiLayerEvent implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(GuiGraphics guiGraphics, float partialTick, ResourceLocation name, LayeredDraw.Layer layer) {
            super(guiGraphics, partialTick, name, layer);
        }
    }

    /**
     * Fired <b>after</b> a GUI layer is rendered to the screen, if the corresponding {@link Pre} is not cancelled.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Post extends RenderGuiLayerEvent {
        @ApiStatus.Internal
        public Post(GuiGraphics guiGraphics, float partialTick, ResourceLocation name, LayeredDraw.Layer layer) {
            super(guiGraphics, partialTick, name, layer);
        }
    }
}
