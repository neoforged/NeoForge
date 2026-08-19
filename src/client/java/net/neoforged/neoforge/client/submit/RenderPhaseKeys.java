/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.submit;

import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.neoforged.neoforge.client.extensions.OrderedSubmitNodeCollectorExtension;

/// Holds the [RenderPhaseKey]s for all vanilla [FeatureRenderPhase]s.
///
/// @see OrderedSubmitNodeCollectorExtension#submitSpecial(RenderPhaseKey, SubmitNode)
public final class RenderPhaseKeys {
    public static final RenderPhaseKey<SubmitNode> SOLID = new RenderPhaseKey<>(collection -> collection.solid);
    public static final RenderPhaseKey<SubmitNode> SHADOWS = new RenderPhaseKey<>(collection -> collection.shadows);
    public static final RenderPhaseKey<SubmitNode> NAME_TAGS = new RenderPhaseKey<>(collection -> collection.nameTags);
    public static final RenderPhaseKey<SubmitNode> TEXTS = new RenderPhaseKey<>(collection -> collection.texts);
    public static final RenderPhaseKey<SubmitNode> SHAPE_OUTLINES = new RenderPhaseKey<>(collection -> collection.shapeOutlines);
    public static final RenderPhaseKey<? super TranslucentSubmit> TRANSLUCENT_BLOCKS_AND_ITEMS = new RenderPhaseKey<>(collection -> collection.translucentBlocksAndItems);
    public static final RenderPhaseKey<? super TranslucentSubmit> TRANSLUCENT_MODELS = new RenderPhaseKey<>(collection -> collection.translucentModels);
    public static final RenderPhaseKey<SubmitNode> TRANSLUCENT_CUSTOM_GEOMETRY = new RenderPhaseKey<>(collection -> collection.translucentCustomGeometry);
    public static final RenderPhaseKey<SubmitNode> TRANSLUCENT_GIZMOS = new RenderPhaseKey<>(collection -> collection.translucentGizmos);
    public static final RenderPhaseKey<SubmitNode> BREAKING_OVERLAY = new RenderPhaseKey<>(collection -> collection.breakingOverlay);
    public static final RenderPhaseKey<SubmitNode> WATER_MASK = new RenderPhaseKey<>(collection -> collection.waterMask);
    public static final RenderPhaseKey<SubmitNode> AFTER_TERRAIN = new RenderPhaseKey<>(collection -> collection.afterTerrain);
    public static final RenderPhaseKey<SubmitNode> ALWAYS_ON_TOP = new RenderPhaseKey<>(collection -> collection.alwaysOnTopGizmos);
    public static final RenderPhaseKey<SubmitNode> OUTLINE = new RenderPhaseKey<>(collection -> collection.outline);

    private RenderPhaseKeys() {}
}
