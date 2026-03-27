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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4fc;

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
    private final CameraRenderState cameraState;
    private final Matrix4fc modelViewMatrix;
    private final DeltaTracker deltaTracker;
    private final ProfilerFiller profiler;
    private boolean enableOutline;

    @ApiStatus.Internal
    public FrameGraphSetupEvent(
            FrameGraphBuilder builder,
            LevelTargetBundle targets,
            RenderTargetDescriptor renderTargetDescriptor,
            CameraRenderState cameraState,
            Matrix4fc modelViewMatrix,
            DeltaTracker deltaTracker,
            ProfilerFiller profiler) {
        this.builder = builder;
        this.targets = targets;
        this.renderTargetDescriptor = renderTargetDescriptor;
        this.frustum = cameraState.cullFrustum;
        this.cameraState = cameraState;
        this.modelViewMatrix = modelViewMatrix;
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
     * {@return the {@link CameraRenderState}} extracted from the active {@link Camera}
     */
    public CameraRenderState getCameraState() {
        return cameraState;
    }

    /**
     * {@return the model view matrix}
     */
    public Matrix4fc getModelViewMatrix() {
        return modelViewMatrix;
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
