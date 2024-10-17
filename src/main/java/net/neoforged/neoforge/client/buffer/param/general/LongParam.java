/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

public record LongParam(long value) implements IGeneralUsageParam<Long> {
    @Override
    public Long getValue() {
        return value;
    }
}
