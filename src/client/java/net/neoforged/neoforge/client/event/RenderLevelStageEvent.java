/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import java.util.function.Consumer;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.IRenderableSection;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

/// Fires at various times during [LevelRenderer#render] and [GameRenderer#renderLevel].
/// Custom render state used in the various stages must be extracted in [ExtractLevelRenderStateEvent] and
/// stored in the provided [LevelRenderState].
///
/// All stages, except [AfterSky] and [AfterLevel], provide an active [RenderPass]. This `RenderPass` must be used instead of
/// creating a custom one within the event handler.
///
/// Buffer uploads and buffer resizes (including using [RenderSystem.AutoStorageIndexBuffer]) for stages with an active
/// `RenderPass` must be performed in [PrepareRenderBuffersEvent].
///
/// To submit custom geometry to the [SubmitNodeCollector] system, use [SubmitCustomGeometryEvent] instead.
///
/// The sub-events are not [cancellable][ICancellableEvent].
///
/// The sub-events are fired on the [main NeoForge event bus][NeoForge#EVENT_BUS],
/// only on the [logical client][LogicalSide#CLIENT].
///
/// The sub-events are fired in the following order:
/// - [AfterSky],
/// - [PrepareRenderBuffersEvent],
/// - [AfterOpaqueBlocks],
/// - [AfterOpaqueFeatures],
/// - If [GameRenderer#useImprovedTransparency()] returns `true`:
///   - [OitTranslucent],
/// - Else:
///   - [AfterTranslucentFeatures]
///   - [AfterTranslucentBlocks],
///   - [AfterTranslucentParticles],
///   - [AfterWeather],
/// - [AfterLevel]
public abstract class RenderLevelStageEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final LevelRenderState levelRenderState;
    private final PoseStack poseStack;
    private final Matrix4fc modelViewMatrix;
    private final Iterable<? extends IRenderableSection> renderableSections;
    @Nullable
    private final RenderPass renderPass;

    public RenderLevelStageEvent(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections, @Nullable RenderPass renderPass) {
        this.levelRenderer = levelRenderer;
        this.levelRenderState = levelRenderState;
        this.poseStack = poseStack != null ? poseStack : new PoseStack();
        this.modelViewMatrix = modelViewMatrix;
        this.renderableSections = renderableSections;
        this.renderPass = renderPass;
    }

    /// {@return the level renderer}
    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    /// {@return the level render state}
    public LevelRenderState getLevelRenderState() {
        return levelRenderState;
    }

    /// {@return the pose stack used for rendering}
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /// {@return the model view matrix used for rendering}
    public Matrix4fc getModelViewMatrix() {
        return modelViewMatrix;
    }

    /// Returns an iterable of all visible sections.
    ///
    /// Calling [Iterable#forEach(Consumer)] on the returned iterable allows the underlying renderer
    /// to optimize how it fetches the visible sections, and is recommended.
    public Iterable<? extends IRenderableSection> getRenderableSections() {
        return renderableSections;
    }

    /// {@return the active [RenderPass] of this render stage, if available}
    ///
    /// The returned render pass must not be closed!
    protected RenderPass getRenderPass() {
        return Objects.requireNonNull(this.renderPass, "This render stage does not provide an active render pass");
    }

    /// Fired at the end of [LevelRenderer#addSkyPass] after the sky has been rendered. This is the first `RenderLevelStageEvent` sub-event to fire.
    public static class AfterSky extends RenderLevelStageEvent {
        public AfterSky(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections, null);
        }
    }

    /// Fired early in [LevelRenderer#executeSolid] after solid and cutout chunk geometry has been rendered.
    public static class AfterOpaqueBlocks extends RenderLevelStageEvent {
        public AfterOpaqueBlocks(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections, RenderPass renderPass) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections, renderPass);
        }

        @Override
        public RenderPass getRenderPass() {
            return super.getRenderPass();
        }
    }

    /// Fired within [LevelRenderer#executeSolid] after opaque "features" from entities, block entities and particles have been rendered.
    public static class AfterOpaqueFeatures extends RenderLevelStageEvent {
        public AfterOpaqueFeatures(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections, RenderPass renderPass) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections, renderPass);
        }

        @Override
        public RenderPass getRenderPass() {
            return super.getRenderPass();
        }
    }

    /// Fired within [LevelRenderer#executeClassicTransparency] after translucent "features" from entities and block entities have been rendered.
    public static class AfterTranslucentFeatures extends RenderLevelStageEvent {
        public AfterTranslucentFeatures(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections, RenderPass renderPass) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections, renderPass);
        }

        @Override
        public RenderPass getRenderPass() {
            return super.getRenderPass();
        }
    }

    /// Fired within [LevelRenderer#executeClassicTransparency] after translucent chunk geometry has been rendered.
    public static class AfterTranslucentBlocks extends RenderLevelStageEvent {
        public AfterTranslucentBlocks(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections, RenderPass renderPass) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections, renderPass);
        }

        @Override
        public RenderPass getRenderPass() {
            return super.getRenderPass();
        }
    }

    /// Fired within [LevelRenderer#executeClassicTransparency] after translucent "features" from particles have been rendered.
    public static class AfterTranslucentParticles extends RenderLevelStageEvent {
        public AfterTranslucentParticles(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections, RenderPass renderPass) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections, renderPass);
        }

        @Override
        public RenderPass getRenderPass() {
            return super.getRenderPass();
        }
    }

    /// Fired within [LevelRenderer#executeClassicTransparency] after weather has been rendered, before world border rendering.
    public static class AfterWeather extends RenderLevelStageEvent {
        public AfterWeather(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections, RenderPass renderPass) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections, renderPass);
        }

        @Override
        public RenderPass getRenderPass() {
            return super.getRenderPass();
        }
    }

    /// Fired within [LevelRenderer#executeOit] for each [OitStage] after translucent chunk geometry, translucent "features", the world border and weather have been rendered.
    public static class OitTranslucent extends RenderLevelStageEvent {
        private final OitStage oitStage;

        public OitTranslucent(LevelRenderer levelRenderer, LevelRenderState levelRenderState, OitStage oitStage, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections, RenderPass renderPass) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections, renderPass);
            this.oitStage = oitStage;
        }

        /// {@return the current OIT stage}
        public OitStage getOitStage() {
            return oitStage;
        }

        @Override
        public RenderPass getRenderPass() {
            return super.getRenderPass();
        }
    }

    /// Fired within [GameRenderer#renderLevel] after [LevelRenderer#render] is called. This is the last `RenderLevelStageEvent` sub-event to fire.
    public static class AfterLevel extends RenderLevelStageEvent {
        public AfterLevel(LevelRenderer levelRenderer, LevelRenderState levelRenderState, @Nullable PoseStack poseStack, Matrix4fc modelViewMatrix, Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections, null);
        }
    }
}
