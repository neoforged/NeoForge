/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
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
 * Fired when the {@linkplain FrameGraphBuilder frame graph} is set up at the start of level rendering, right before
 * the vanilla frame passes are set up.
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable}.
 * <p>
 * This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public final class FrameGraphSetupEvent extends Event {
    private final FrameGraphBuilder builder;
    private final LevelTargetBundle targets;
    private final RenderTargetDescriptor renderTargetDescriptor;
    private final Frustum frustum;
    private final Camera camera;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f projectionMatrix;
    private final DeltaTracker deltaTracker;
    private final ProfilerFiller profiler;
    private boolean enableOutline;

    @ApiStatus.Internal
    public FrameGraphSetupEvent(
            FrameGraphBuilder builder,
            LevelTargetBundle targets,
            RenderTargetDescriptor renderTargetDescriptor,
            Frustum frustum,
            Camera camera,
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            DeltaTracker deltaTracker,
            ProfilerFiller profiler) {
        this.builder = builder;
        this.targets = targets;
        this.renderTargetDescriptor = renderTargetDescriptor;
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
     * {@return the render target descriptor to use for creating full-screen render targets}
     */
    public RenderTargetDescriptor getRenderTargetDescriptor() {
        return renderTargetDescriptor;
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
