/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.neoforged.neoforge.client.buffer.param.general.FloatParam;

public class ViewOffsetParam {
    public static final class Vanilla {
        public static final FloatParam VIEW_OFFSET_Z_LAYERING = new FloatParam(1.0f);
        public static final FloatParam VIEW_OFFSET_Z_LAYERING_FORWARD = new FloatParam(-1.0f);
    }
}
