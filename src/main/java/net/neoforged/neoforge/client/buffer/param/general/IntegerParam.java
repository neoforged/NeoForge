/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

public record IntegerParam(int value) implements IGeneralUsageParam<Integer> {
    @Override
    public Integer getValue() {
        return value;
    }
}
