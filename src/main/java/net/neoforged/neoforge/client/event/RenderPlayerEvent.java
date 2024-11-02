/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.neoforged.bus.api.Event;
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
 * @see PlayerRenderer
 */
public abstract class RenderPlayerEvent extends RenderLivingEvent<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
    @ApiStatus.Internal
    protected RenderPlayerEvent(PlayerRenderState renderState, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        super(renderState, renderer, partialTick, poseStack, multiBufferSource, packedLight);
    }

    /**
     * Fired <b>before</b> the player is rendered.
     * This can be used for rendering additional effects or suppressing rendering.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain Event.HasResult have a result}.
     * If this event is cancelled, then the player will not be rendered and the corresponding
     * {@link RenderPlayerEvent.Post} will not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Pre extends RenderPlayerEvent implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(PlayerRenderState renderState, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(renderState, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    /**
     * Fired <b>after</b> the player is rendered, if the corresponding {@link RenderPlayerEvent.Pre} is not cancelled.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain Event.HasResult have a result}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Post extends RenderPlayerEvent {
        @ApiStatus.Internal
        public Post(PlayerRenderState renderState, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(renderState, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }
}
