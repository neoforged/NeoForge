/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.IRenderableSection;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event can be used to submit custom geometry outside of {@link BlockEntityRenderer}s,
 * {@link EntityRenderer}s and {@link Particle}s.
 * Custom render state used by the submits must be extracted in {@link ExtractLevelRenderStateEvent} and
 * stored in the provided {@link LevelRenderState}
 *
 * <p>This event is fired between particle submission and rendering of opaque submits.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus}, only on the
 * {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public final class SubmitCustomGeometryEvent extends Event {
    private final LevelRenderState levelRenderState;
    private final SubmitNodeCollector submitNodeCollector;
    private final PoseStack poseStack;
    private final Iterable<? extends IRenderableSection> renderableSections;

    @ApiStatus.Internal
    public SubmitCustomGeometryEvent(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, Iterable<? extends IRenderableSection> renderableSections) {
        this.levelRenderState = levelRenderState;
        this.submitNodeCollector = submitNodeCollector;
        this.poseStack = poseStack;
        this.renderableSections = renderableSections;
    }

    /**
     * {@return the level render state}
     */
    public LevelRenderState getLevelRenderState() {
        return levelRenderState;
    }

    /**
     * {@return the submit node collector to submit the geometry to}
     */
    public SubmitNodeCollector getSubmitNodeCollector() {
        return submitNodeCollector;
    }

    /**
     * {@return the pose stack to use for geometry submission}
     */
    public PoseStack getPoseStack() {
        return poseStack;
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
}
