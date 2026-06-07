/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.submit;

import java.util.function.Function;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.neoforged.neoforge.client.extensions.OrderedSubmitNodeCollectorExtension;

/// Identifies a specific [FeatureRenderPhase] in a typesafe way and can resolve it from a [SubmitNodeCollection].
///
/// @see OrderedSubmitNodeCollectorExtension#submitSpecial(RenderPhaseKey, SubmitNode)
public final class RenderPhaseKey<T extends SubmitNode> {
    private final Function<SubmitNodeCollection, FeatureRenderPhase<T>> phaseGetter;

    public RenderPhaseKey(Function<SubmitNodeCollection, FeatureRenderPhase<T>> phaseGetter) {
        this.phaseGetter = phaseGetter;
    }

    public FeatureRenderPhase<T> resolve(SubmitNodeCollection collection) {
        return this.phaseGetter.apply(collection);
    }
}
