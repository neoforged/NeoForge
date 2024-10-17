/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

public record ShortParam(short value) implements IGeneralUsageParam<Short> {
    @Override
    public Short getValue() {
        return value;
    }
}
