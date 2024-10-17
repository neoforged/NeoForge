/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

import org.joml.Vector3f;

public record Vector3fParam(Vector3f vector3f) implements IGeneralUsageParam<Vector3f> {
    @Override
    public Vector3f getValue() {
        return vector3f;
    }
}
