/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

public record BooleanParam(boolean value) implements IGeneralUsageParam<Boolean> {
    @Override
    public Boolean getValue() {
        return value;
    }
}
