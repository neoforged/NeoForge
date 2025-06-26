/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.IRenderableSection;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Fires at various times during {@linkplain LevelRenderer#renderLevel} and {@linkplain GameRenderer#renderLevel}
 *
 * <p>The sub-events are not {@linkplain ICancellableEvent cancellable}. </p>
 *
 * <p>The sub-events are fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.
 *
 * <p>The current order that the sub-events fire in are:
 * {@code AfterSky},
 * {@code AfterOpaqueBlocks},
 * {@code AfterEntities},
 * {@code AfterBlockEntities},
 * {@code AfterTranslucentBlocks},
 * {@code AfterTripwireBlocks},
 * {@code AfterParticles},
 * {@code AfterWeather},
 * {@code AfterLevel}
 */
public abstract class RenderLevelStageEvent extends Event {
    private final Level level;
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final Matrix4f modelViewMatrix;
    private final int renderTick;
    private final DeltaTracker partialTick;
    private final Camera camera;
    private final Frustum frustum;
    private final Iterable<? extends IRenderableSection> renderableSections;

    public RenderLevelStageEvent(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
        this.level = level;
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack != null ? poseStack : new PoseStack();
        this.modelViewMatrix = modelViewMatrix;
        this.renderTick = renderTick;
        this.partialTick = partialTick;
        this.camera = camera;
        this.frustum = frustum;
        this.renderableSections = renderableSections;
    }

    /**
     * {@return the current {@linkplain Level level} that is being rendered.}
     */
    public Level getLevel() {
        return level;
    }

    /**
     * {@return the level renderer}
     */
    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    /**
     * {@return the pose stack used for rendering}
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * {@return the model view matrix used for rendering}
     */
    public Matrix4f getModelViewMatrix() {
        return modelViewMatrix;
    }

    /**
     * {@return the current "ticks" value in the {@linkplain LevelRenderer level renderer}}
     */
    public int getRenderTick() {
        return renderTick;
    }

    /**
     * {@return the current partialTick value used for rendering}
     */
    public DeltaTracker getPartialTick() {
        return partialTick;
    }

    /**
     * {@return the camera}
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * {@return the frustum}
     */
    public Frustum getFrustum() {
        return frustum;
    }

    /**
     * Returns an iterable of all visible sections.
     * <p>
     * Calling {@link Iterable#forEach(Consumer)} on the returned iterable allows the underlying renderer
     * to optimize how it fetches the visible sections, and is recommended.
     */
    public Iterable<? extends IRenderableSection> getRenderableSections() {
        return renderableSections;
    }

    /**
     * Fired at the end of {@linkplain LevelRenderer#addSkyPass}. This is the first RenderLevelStageEvent sub-event to fire.
     */
    public static class AfterSky extends RenderLevelStageEvent {
        public AfterSky(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
            super(level, levelRenderer, poseStack, modelViewMatrix, renderTick, partialTick, camera, frustum, renderableSections);
        }
    }

    /**
     * Fired early in {@linkplain LevelRenderer#addMainPass} after {@code AfterSky} had ran.
     */
    public static class AfterOpaqueBlocks extends RenderLevelStageEvent {
        public AfterOpaqueBlocks(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
            super(level, levelRenderer, poseStack, modelViewMatrix, renderTick, partialTick, camera, frustum, renderableSections);
        }
    }

    /**
     * Fired within {@linkplain LevelRenderer#addMainPass} after {@code AfterOpaqueBlocks} had ran.
     */
    public static class AfterEntities extends RenderLevelStageEvent {
        public AfterEntities(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
            super(level, levelRenderer, poseStack, modelViewMatrix, renderTick, partialTick, camera, frustum, renderableSections);
        }
    }

    /**
     * Fired within {@linkplain LevelRenderer#addMainPass} after {@code AfterEntities} had ran and just before block outline rendering.
     */
    public static class AfterBlockEntities extends RenderLevelStageEvent {
        public AfterBlockEntities(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
            super(level, levelRenderer, poseStack, modelViewMatrix, renderTick, partialTick, camera, frustum, renderableSections);
        }
    }

    /**
     * Fired within {@linkplain LevelRenderer#addMainPass} after {@code AfterBlockEntities} had ran.
     */
    public static class AfterTranslucentBlocks extends RenderLevelStageEvent {
        public AfterTranslucentBlocks(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
            super(level, levelRenderer, poseStack, modelViewMatrix, renderTick, partialTick, camera, frustum, renderableSections);
        }
    }

    /**
     * Fired near the end of {@linkplain LevelRenderer#addMainPass} after {@code AfterTranslucentBlocks} had ran.
     */
    public static class AfterTripwireBlocks extends RenderLevelStageEvent {
        public AfterTripwireBlocks(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
            super(level, levelRenderer, poseStack, modelViewMatrix, renderTick, partialTick, camera, frustum, renderableSections);
        }
    }

    /**
     * Fired at the end of {@linkplain LevelRenderer#addParticlesPass} after {@code AfterTripwireBlocks} had ran.
     */
    public static class AfterParticles extends RenderLevelStageEvent {
        public AfterParticles(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
            super(level, levelRenderer, poseStack, modelViewMatrix, renderTick, partialTick, camera, frustum, renderableSections);
        }
    }

    /**
     * Fired near the end of {@linkplain LevelRenderer#addWeatherPass} after {@code AfterParticles} had ran but before world border rendering.
     */
    public static class AfterWeather extends RenderLevelStageEvent {
        public AfterWeather(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
            super(level, levelRenderer, poseStack, modelViewMatrix, renderTick, partialTick, camera, frustum, renderableSections);
        }
    }

    /**
     * Fired within {@linkplain GameRenderer#renderLevel} after {@linkplain LevelRenderer#renderLevel} is called. This is the last RenderLevelStageEvent sub-event to fire.
     */
    public static class AfterLevel extends RenderLevelStageEvent {
        public AfterLevel(Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
            super(level, levelRenderer, poseStack, modelViewMatrix, renderTick, partialTick, camera, frustum, renderableSections);
        }
    }
}
