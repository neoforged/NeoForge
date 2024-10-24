/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.neoforged.neoforge.client.buffer.param.general.FloatParam;

public class GlintScaleParam {
    public static final class Vanilla {
        public static final FloatParam GLINT_TEXTURING = new FloatParam(8.0f);
        public static final FloatParam ENTITY_GLINT_TEXTURING = new FloatParam(0.16f);
    }
}
