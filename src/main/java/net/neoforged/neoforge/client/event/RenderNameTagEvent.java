/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is fired before an entity renderer renders the nameplate of an entity.
 * It allows reacting to the render and controlling if the name plate will be rendered, as well as changing the rendered name.
 * <p>
 * This event is only fired on the logical client.
 *
 * @see EntityRenderer
 */
public class RenderNameTagEvent extends EntityEvent {
    private final Component originalContent;
    private final EntityRenderer<?> entityRenderer;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;
    private final float partialTick;

    private Component content;
    private TriState canRender = TriState.DEFAULT;

    @ApiStatus.Internal
    public RenderNameTagEvent(Entity entity, Component content, EntityRenderer<?> entityRenderer, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, float partialTick) {
        super(entity);
        this.originalContent = content;
        this.setContent(this.originalContent);
        this.entityRenderer = entityRenderer;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
        this.partialTick = partialTick;
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
    public Component getContent() {
        return this.content;
    }

    /**
     * {@return the original text on the nameplate}
     */
    public Component getOriginalContent() {
        return this.originalContent;
    }

    /**
     * {@return the entity renderer rendering the nameplate}
     */
    public EntityRenderer<?> getEntityRenderer() {
        return this.entityRenderer;
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
    public MultiBufferSource getMultiBufferSource() {
        return this.multiBufferSource;
    }

    /**
     * {@return the amount of packed (sky and block) light for rendering}
     *
     * @see net.minecraft.client.renderer.LightTexture
     */
    public int getPackedLight() {
        return this.packedLight;
    }

    /**
     * {@return the partial tick}
     */
    public float getPartialTick() {
        return this.partialTick;
    }
}
