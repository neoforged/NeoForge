/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired before a selection highlight is rendered.
 * See the two subclasses to listen for blocks or entities.
 *
 * @see Block
 * @see Entity
 */
public abstract class RenderHighlightEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final Camera camera;
    private final HitResult target;
    private final DeltaTracker deltaTracker;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;

    @ApiStatus.Internal
    protected RenderHighlightEvent(LevelRenderer levelRenderer, Camera camera, HitResult target, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        this.levelRenderer = levelRenderer;
        this.camera = camera;
        this.target = target;
        this.deltaTracker = deltaTracker;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
    }

    /**
     * {@return the level renderer}
     */
    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    /**
     * {@return the camera information}
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * {@return the hit result which triggered the selection highlight}
     */
    public HitResult getTarget() {
        return target;
    }

    /**
     * {@return the delta tracker}
     */
    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    /**
     * {@return the pose stack used for rendering}
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * {@return the source of rendering buffers}
     */
    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }

    /**
     * Fired before a block's selection highlight is rendered.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
     * If the event is cancelled, then the selection highlight will not be rendered.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Block extends RenderHighlightEvent implements ICancellableEvent {
        @ApiStatus.Internal
        public Block(LevelRenderer levelRenderer, Camera camera, BlockHitResult target, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource bufferSource) {
            super(levelRenderer, camera, target, deltaTracker, poseStack, bufferSource);
        }

        /**
         * {@return the block hit result}
         */
        @Override
        public BlockHitResult getTarget() {
            return (BlockHitResult) super.target;
        }
    }

    /**
     * Fired before an entity's selection highlight is rendered.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Entity extends RenderHighlightEvent {
        @ApiStatus.Internal
        public Entity(LevelRenderer levelRenderer, Camera camera, EntityHitResult target, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource bufferSource) {
            super(levelRenderer, camera, target, deltaTracker, poseStack, bufferSource);
        }

        /**
         * {@return the entity hit result}
         */
        @Override
        public EntityHitResult getTarget() {
            return (EntityHitResult) super.target;
        }
    }
}
