/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired before an entity renderer renders the nameplate of an entity.
 * <p>
 * This event is only fired on the logical client.
 *
 * @see EntityRenderer
 */
public abstract class RenderNameTagEvent extends Event {
    private final EntityRenderState renderState;
    private final EntityRenderer<?, ?> entityRenderer;
    private final float partialTick;

    @ApiStatus.Internal
    public RenderNameTagEvent(EntityRenderState renderState, EntityRenderer<?, ?> entityRenderer, float partialTick) {
        this.renderState = renderState;
        this.entityRenderer = entityRenderer;
        this.partialTick = partialTick;
    }

    /**
     * {@return the render state of the entity whose nameplate is being rendered}
     */
    public EntityRenderState getEntityRenderState() {
        return renderState;
    }

    /**
     * {@return the entity renderer rendering the nameplate}
     */
    public EntityRenderer<?, ?> getEntityRenderer() {
        return this.entityRenderer;
    }

    /**
     * {@return the partial tick}
     */
    public float getPartialTick() {
        return this.partialTick;
    }

    /**
     * This event is fired when an entity renderer extracts the render state of an entity relevant to rendering the nametag.
     * It allows controlling whether the name plate will be rendered, as well as changing the rendered name.
     * <p>
     * This event is only fired on the logical client on the {@link NeoForge#EVENT_BUS}.
     *
     * @see EntityRenderer
     */
    public static class CanRender extends RenderNameTagEvent {
        private final Entity entity;
        @Nullable
        private final Component originalContent;
        @Nullable
        private Component content;
        private TriState canRender = TriState.DEFAULT;

        public CanRender(Entity entity, EntityRenderState renderState, @Nullable Component content, EntityRenderer<?, ?> entityRenderer, float partialTick) {
            super(renderState, entityRenderer, partialTick);
            this.entity = entity;
            this.originalContent = content;
            this.content = content;
        }

        /**
         * {@return the entity whose nameplate is being rendered}
         */
        public Entity getEntity() {
            return entity;
        }

        /**
         * {@return the original text on the nameplate}
         */
        @Nullable
        public Component getOriginalContent() {
            return this.originalContent;
        }

        /**
         * Changes if the {@link #getContent() content} of the nameplate will be rendered.
         * {@link TriState#TRUE} and {@link TriState#FALSE} will allow/deny the render respectively.
         * <p>
         * Using {@link TriState#DEFAULT} will cause the name to render if {@link EntityRenderer#shouldShowName} returns true.
         */
        public void setCanRender(TriState canRender) {
            this.canRender = canRender;
        }

        /**
         * {@return if the nameplate will render or not}
         */
        public TriState canRender() {
            return canRender;
        }

        /**
         * Sets the new text on the nameplate.
         *
         * @param contents the new text
         */
        public void setContent(Component contents) {
            this.content = contents;
        }

        /**
         * {@return the text on the nameplate that will be rendered}
         */
        @Nullable
        public Component getContent() {
            return this.content;
        }
    }

    /**
     * This event is fired before an entity renderer renders the nameplate of an entity.
     * <p>
     * It allows reacting to the rendering as well as performing custom rendering and preventing
     * the vanilla rendering.
     * <p>
     * This event is only fired on the logical client on the {@link NeoForge#EVENT_BUS}.
     *
     * @see EntityRenderer
     */
    public static class DoRender extends RenderNameTagEvent implements ICancellableEvent {
        private final Component content;
        private final PoseStack poseStack;
        private final SubmitNodeCollector submitNodeCollector;
        private final CameraRenderState cameraRenderState;

        public DoRender(EntityRenderState renderState, Component content, EntityRenderer<?, ?> entityRenderer, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, float partialTick) {
            super(renderState, entityRenderer, partialTick);
            this.content = content;
            this.poseStack = poseStack;
            this.submitNodeCollector = submitNodeCollector;
            this.cameraRenderState = cameraRenderState;
        }

        /**
         * {@return the text on the nameplate}
         */
        public Component getContent() {
            return this.content;
        }

        /**
         * {@return the pose stack used for rendering}
         */
        public PoseStack getPoseStack() {
            return this.poseStack;
        }

        /**
         * {@return the source of rendering buffers}
         */
        public SubmitNodeCollector getSubmitNodeCollector() {
            return this.submitNodeCollector;
        }

        /**
         * {@return the render state of the camera from which the name tag is being observed}
         */
        public CameraRenderState getCameraRenderState() {
            return cameraRenderState;
        }
    }
}
