/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

/**
 * Fired when the {@linkplain FrameGraphBuilder frame graph} is set up at the start of level rendering.
 * <p>
 * These events are not {@linkplain ICancellableEvent cancellable}.
 * <p>
 * These events are fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public abstract sealed class FrameGraphSetupEvent extends Event {
    protected final FrameGraphBuilder builder;
    private final LevelTargetBundle targets;
    private final Frustum frustum;
    private final Camera camera;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f projectionMatrix;
    private final DeltaTracker deltaTracker;
    private final ProfilerFiller profiler;

    protected FrameGraphSetupEvent(
            FrameGraphBuilder builder,
            LevelTargetBundle targets,
            Frustum frustum,
            Camera camera,
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            DeltaTracker deltaTracker,
            ProfilerFiller profiler) {
        this.builder = builder;
        this.targets = targets;
        this.frustum = frustum;
        this.camera = camera;
        this.modelViewMatrix = modelViewMatrix;
        this.projectionMatrix = projectionMatrix;
        this.deltaTracker = deltaTracker;
        this.profiler = profiler;
    }

    /**
     * {@return the {@link FrameGraphBuilder} used to set up the frame graph}
     */
    public FrameGraphBuilder getFrameGrapBuilder() {
        return builder;
    }

    /**
     * {@return the render targets used during level rendering}
     */
    public LevelTargetBundle getTargetBundle() {
        return targets;
    }

    /**
     * {@return the culling frustum}
     */
    public Frustum getFrustum() {
        return frustum;
    }

    /**
     * {@return the active {@link Camera}}
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * {@return the model view matrix}
     */
    public Matrix4f getModelViewMatrix() {
        return modelViewMatrix;
    }

    /**
     * {@return the projection matrix}
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * {@return the {@link DeltaTracker}}
     */
    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    /**
     * {@return the active {@linkplain ProfilerFiller profiler}}
     */
    public ProfilerFiller getProfiler() {
        return profiler;
    }

    /**
     * Fired at the start of frame graph setup, right after the "clear" pass is added
     */
    public static final class Pre extends FrameGraphSetupEvent {
        private boolean enableOutline;

        @ApiStatus.Internal
        public Pre(
                FrameGraphBuilder builder,
                LevelTargetBundle targets,
                Frustum frustum,
                Camera camera,
                Matrix4f modelViewMatrix,
                Matrix4f projectionMatrix,
                DeltaTracker deltaTracker,
                ProfilerFiller profiler) {
            super(builder, targets, frustum, camera, modelViewMatrix, projectionMatrix, deltaTracker, profiler);
        }

        /**
         * Enables the entity outline post-processing shader regardless of any entities having active outlines
         */
        public void enableOutlineProcessing() {
            this.enableOutline = true;
        }

        /**
         * {@return whether the entity outline post-processing shader will be enabled regardless of entities using it}
         */
        public boolean isOutlineProcessingEnabled() {
            return enableOutline;
        }
    }

    /**
     * Fired at the end of frame graph setup, right before the frame graph is executed
     */
    public static final class Post extends FrameGraphSetupEvent {
        @ApiStatus.Internal
        public Post(
                FrameGraphBuilder builder,
                LevelTargetBundle targets,
                Frustum frustum,
                Camera camera,
                Matrix4f modelViewMatrix,
                Matrix4f projectionMatrix,
                DeltaTracker deltaTracker,
                ProfilerFiller profiler) {
            super(builder, targets, frustum, camera, modelViewMatrix, projectionMatrix, deltaTracker, profiler);
        }
    }
}
