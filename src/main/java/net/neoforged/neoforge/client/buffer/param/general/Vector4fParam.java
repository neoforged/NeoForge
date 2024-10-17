/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

import org.joml.Vector4f;

public record Vector4fParam(Vector4f vector4f) implements IGeneralUsageParam<Vector4f> {
    @Override
    public Vector4f getValue() {
        return vector4f;
    }
}
