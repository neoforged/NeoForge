/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.neoforged.neoforge.client.buffer.param.general.Vector3fParam;
import org.joml.Vector3f;

public class ViewOffsetParam {
    public static final class Vanilla {
        public static final Vector3fParam VIEW_OFFSET_Z_LAYERING = new Vector3fParam(new Vector3f(0.99975586f, 0.99975586f, 0.99975586f));
    }
}
