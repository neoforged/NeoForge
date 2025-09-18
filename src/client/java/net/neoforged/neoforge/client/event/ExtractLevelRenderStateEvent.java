/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.context.ContextKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.extensions.IDimensionSpecialEffectsExtension;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when the {@link LevelRenderer} extracts level render state, after all vanilla states have been extracted.
 * Use this event to extract custom render state for use in {@link RenderLevelStageEvent} or {@link IDimensionSpecialEffectsExtension}.
 * Custom data can be stored on and retrieved from the provided {@link LevelRenderState} via {@link LevelRenderState#setRenderData(ContextKey, Object)}
 * and {@link LevelRenderState#getRenderData(ContextKey)} respectively.
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable}.
 * <p>
 * This event is fired on the {@linkplain NeoForge#EVENT_BUS main game event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public final class ExtractLevelRenderStateEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final LevelRenderState renderState;
    private final ClientLevel level;
    private final Camera camera;
    private final Frustum frustum;
    private final DeltaTracker deltaTracker;
    private final int renderTick;

    @ApiStatus.Internal
    public ExtractLevelRenderStateEvent(
            LevelRenderer levelRenderer,
            LevelRenderState renderState,
            ClientLevel level,
            Camera camera,
            Frustum frustum,
            DeltaTracker deltaTracker,
            int renderTick) {
        this.levelRenderer = levelRenderer;
        this.renderState = renderState;
        this.level = level;
        this.camera = camera;
        this.frustum = frustum;
        this.deltaTracker = deltaTracker;
        this.renderTick = renderTick;
    }

    /**
     * {@return the {@link LevelRenderer} performing the extraction}
     */
    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    /**
     * {@return the {@link LevelRenderState} being extracted to}
     */
    public LevelRenderState getRenderState() {
        return renderState;
    }

    /**
     * {@return the {@link ClientLevel} whose state is being extracted}
     */
    public ClientLevel getLevel() {
        return level;
    }

    /**
     * {@return the {@link Camera} from which the world is being observed}
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * {@return the active {@link Frustum} used for culling}
     */
    public Frustum getFrustum() {
        return frustum;
    }

    /**
     * {@return the {@link DeltaTracker} providing partial tick and delta ticks}
     */
    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    /**
     * {@return the current "ticks" value in the {@link LevelRenderer}}
     */
    public int getRenderTick() {
        return this.renderTick;
    }
}
