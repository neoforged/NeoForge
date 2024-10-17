/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

public record ByteParam(byte value) implements IGeneralUsageParam<Byte> {
    @Override
    public Byte getValue() {
        return value;
    }
}
