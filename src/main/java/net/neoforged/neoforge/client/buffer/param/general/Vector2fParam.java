/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

import org.joml.Vector2f;

public record Vector2fParam(Vector2f vector2f) implements IGeneralUsageParam<Vector2f> {
    @Override
    public Vector2f getValue() {
        return vector2f;
    }
}
