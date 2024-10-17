/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

public record DoubleParam(double value) implements IGeneralUsageParam<Double> {
    @Override
    public Double getValue() {
        return value;
    }
}
