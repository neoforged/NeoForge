/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import java.util.OptionalDouble;

public record LineParam(OptionalDouble width) implements IBufferDefinitionParam<OptionalDouble> {

    @Override
    public OptionalDouble getValue() {
        return width;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypes.LINE;
    }
    public static final class Vanilla {
        public static final LineParam DEFAULT_LINE = new LineParam(OptionalDouble.of(1.0));
    }
}
