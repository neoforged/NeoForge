/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.neoforged.neoforge.client.buffer.param.general.Vector2fParam;
import org.joml.Vector2f;

public class PolygonOffsetParam {
    public static final class Vanilla {
        public static final Vector2fParam POLYGON_OFFSET_LAYERING = new Vector2fParam(new Vector2f(-1.0f, -10.0f));
    }
}
