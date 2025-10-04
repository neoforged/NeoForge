/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.IRenderableSection;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Fires at various times during {@linkplain LevelRenderer#renderLevel} and {@linkplain GameRenderer#renderLevel}.
 * Custom render state used in the various stages must be extracted in {@link ExtractLevelRenderStateEvent} and
 * stored in the provided {@link LevelRenderState}
 *
 * <p>The sub-events are not {@linkplain ICancellableEvent cancellable}. </p>
 *
 * <p>The sub-events are fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.
 *
 * <p>The sub-events are fired in the following order:
 * {@link AfterSky},
 * {@link AfterOpaqueBlocks},
 * {@link AfterEntities},
 * {@link AfterTranslucentBlocks},
 * {@link AfterTripwireBlocks},
 * {@link AfterParticles},
 * {@link AfterWeather},
 * {@link AfterLevel}
 */
public abstract class RenderLevelStageEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final LevelRenderState levelRenderState;
    private final PoseStack poseStack;
    private final Matrix4f modelViewMatrix;
    private final Iterable<? extends IRenderableSection> renderableSections;

    public RenderLevelStageEvent(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
        this.levelRenderer = levelRenderer;
        this.levelRenderState = levelRenderState;
        this.poseStack = poseStack != null ? poseStack : new PoseStack();
        this.modelViewMatrix = modelViewMatrix;
        this.renderableSections = renderableSections;
    }

    /**
     * {@return the level renderer}
     */
    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    /**
     * {@return the level render state}
     */
    public LevelRenderState getLevelRenderState() {
        return levelRenderState;
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
     * Returns an iterable of all visible sections.
     * <p>
     * Calling {@link Iterable#forEach(Consumer)} on the returned iterable allows the underlying renderer
     * to optimize how it fetches the visible sections, and is recommended.
     */
    public Iterable<? extends IRenderableSection> getRenderableSections() {
        return renderableSections;
    }

    /**
     * Fired at the end of {@linkplain LevelRenderer#addSkyPass} after the sky has been rendered. This is the first RenderLevelStageEvent sub-event to fire.
     */
    public static class AfterSky extends RenderLevelStageEvent {
        public AfterSky(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    /**
     * Fired early in {@linkplain LevelRenderer#addMainPass} after solid and cutout chunk geometry has been rendered.
     */
    public static class AfterOpaqueBlocks extends RenderLevelStageEvent {
        public AfterOpaqueBlocks(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    /**
     * Fired within {@linkplain LevelRenderer#addMainPass} after entities and block entities have been rendered.
     */
    public static class AfterEntities extends RenderLevelStageEvent {
        public AfterEntities(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    /**
     * Fired within {@linkplain LevelRenderer#addMainPass} after translucent chunk geometry has been rendered.
     */
    public static class AfterTranslucentBlocks extends RenderLevelStageEvent {
        public AfterTranslucentBlocks(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    /**
     * Fired near the end of {@linkplain LevelRenderer#addMainPass} after tripwire chunk geometry has been rendered.
     */
    public static class AfterTripwireBlocks extends RenderLevelStageEvent {
        public AfterTripwireBlocks(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    /**
     * Fired at the end of {@linkplain LevelRenderer#addParticlesPass} after particles have been rendered.
     */
    public static class AfterParticles extends RenderLevelStageEvent {
        public AfterParticles(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    /**
     * Fired near the end of {@linkplain LevelRenderer#addWeatherPass} after weather has been rendered, before world border rendering.
     */
    public static class AfterWeather extends RenderLevelStageEvent {
        public AfterWeather(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    /**
     * Fired within {@linkplain GameRenderer#renderLevel} after {@linkplain LevelRenderer#renderLevel} is called. This is the last RenderLevelStageEvent sub-event to fire.
     */
    public static class AfterLevel extends RenderLevelStageEvent {
        public AfterLevel(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }
}
