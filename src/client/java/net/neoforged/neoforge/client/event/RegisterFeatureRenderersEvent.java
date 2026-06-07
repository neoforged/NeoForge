/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/// Fired when a [FeatureRenderDispatcher] collects all [FeatureRenderer]s for mods to register renderers
/// for custom [SubmitNode]s.
///
/// This event is not [cancelable][ICancellableEvent].
///
/// This event fires on the [game event bus][NeoForge#EVENT_BUS] only on the [logical client][LogicalSide#CLIENT].
public final class RegisterFeatureRenderersEvent extends Event {
    private final FeatureRendererMap featureRenderers;

    @ApiStatus.Internal
    public RegisterFeatureRenderersEvent(FeatureRendererMap featureRenderers) {
        this.featureRenderers = featureRenderers;
    }

    /// Register the provided [FeatureRenderer] for [SubmitNode]s referencing the given [FeatureRendererType].
    ///
    /// @param type     The type referenced by the [SubmitNode]s
    /// @param renderer The feature renderer to use for rendering the [SubmitNode]s
    public <S extends SubmitNode> void register(FeatureRendererType<S> type, FeatureRenderer<S> renderer) {
        if (this.featureRenderers.get(type) != null) {
            throw new IllegalStateException("Duplicate renderer registration for " + type);
        }
        this.featureRenderers.put(type, renderer);
    }
}
