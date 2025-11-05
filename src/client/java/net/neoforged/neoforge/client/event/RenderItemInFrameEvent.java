/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired before an item stack is rendered in an item frame.
 * This can be used to prevent normal rendering or add custom rendering.
 *
 * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 * If the event is cancelled, then the item stack will not be rendered</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 *
 * @see ItemFrameRenderer
 */
public class RenderItemInFrameEvent extends Event implements ICancellableEvent {
    private final ItemStackRenderState itemStack;
    private final ItemFrameRenderState frameRenderState;
    private final ItemFrameRenderer<?> renderer;
    private final PoseStack poseStack;
    private final SubmitNodeCollector submitNodeCollector;

    @ApiStatus.Internal
    public RenderItemInFrameEvent(ItemFrameRenderState frameRenderState, ItemFrameRenderer<?> renderItemFrame, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        this.itemStack = frameRenderState.item;
        this.frameRenderState = frameRenderState;
        this.renderer = renderItemFrame;
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
    }

    /**
     * {@return the item stack being rendered}
     */
    public ItemStackRenderState getItemStackRenderState() {
        return itemStack;
    }

    /**
     * {@return the item frame entity}
     */
    public ItemFrameRenderState getItemFrameRenderState() {
        return frameRenderState;
    }

    /**
     * {@return the renderer for the item frame entity}
     */
    public ItemFrameRenderer<?> getRenderer() {
        return renderer;
    }

    /**
     * {@return the pose stack used for rendering}
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * {@return the submit node collector}
     */
    public SubmitNodeCollector getSubmitNodeCollector() {
        return submitNodeCollector;
    }
}
