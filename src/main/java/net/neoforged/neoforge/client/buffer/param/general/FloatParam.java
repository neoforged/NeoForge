/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

public record FloatParam(float value) implements IGeneralUsageParam<Float> {
    @Override
    public Float getValue() {
        return value;
    }
}
