/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.state;

import com.mojang.blaze3d.platform.GlStateManager;

public record TransparencyState(GlStateManager.SourceFactor sourceRgbFactor, GlStateManager.DestFactor destRgbFactor, GlStateManager.SourceFactor sourceAlphaFactor, GlStateManager.DestFactor destAlphaFactor) {

    public TransparencyState(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
        this(sourceFactor, destFactor, sourceFactor, destFactor);
    }
    public static final class Vanilla {
        public static final TransparencyState ADDITIVE_TRANSPARENCY = new TransparencyState(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        public static final TransparencyState LIGHTNING_TRANSPARENCY = new TransparencyState(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        public static final TransparencyState GLINT_TRANSPARENCY = new TransparencyState(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        public static final TransparencyState CRUMBLING_TRANSPARENCY = new TransparencyState(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        public static final TransparencyState TRANSLUCENT_TRANSPARENCY = new TransparencyState(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }
}
