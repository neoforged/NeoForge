/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

public record NameParam(String name) implements IBufferDefinitionParam<String> {
    @Override
    public String getValue() {
        return name;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypeManager.NAME;
    }
}
