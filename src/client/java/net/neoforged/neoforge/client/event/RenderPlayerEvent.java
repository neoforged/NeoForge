/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a player is being rendered.
 * See the two subclasses for listening for before and after rendering.
 *
 * @see RenderPlayerEvent.Pre
 * @see RenderPlayerEvent.Post
 * @see AvatarRenderer
 */
public abstract class RenderPlayerEvent<T extends Avatar & ClientAvatarEntity> extends RenderLivingEvent<T, AvatarRenderState, PlayerModel> {
    @ApiStatus.Internal
    protected RenderPlayerEvent(AvatarRenderState renderState, AvatarRenderer<T> renderer, float partialTick, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        super(renderState, renderer, partialTick, poseStack, submitNodeCollector);
    }

    @Override
    public AvatarRenderer<T> getRenderer() {
        return (AvatarRenderer<T>) super.getRenderer();
    }

    /**
     * Fired <b>before</b> the player is rendered.
     * This can be used for rendering additional effects or suppressing rendering.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}.
     * If this event is cancelled, then the player will not be rendered and the corresponding
     * {@link RenderPlayerEvent.Post} will not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main game event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Pre<T extends Avatar & ClientAvatarEntity> extends RenderPlayerEvent<T> implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(AvatarRenderState renderState, AvatarRenderer<T> renderer, float partialTick, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
            super(renderState, renderer, partialTick, poseStack, submitNodeCollector);
        }
    }

    /**
     * Fired <b>after</b> the player is rendered, if the corresponding {@link RenderPlayerEvent.Pre} is not cancelled.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main game event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Post<T extends Avatar & ClientAvatarEntity> extends RenderPlayerEvent<T> {
        @ApiStatus.Internal
        public Post(AvatarRenderState renderState, AvatarRenderer<T> renderer, float partialTick, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
            super(renderState, renderer, partialTick, poseStack, submitNodeCollector);
        }
    }
}
