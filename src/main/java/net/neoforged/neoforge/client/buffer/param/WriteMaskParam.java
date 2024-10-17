/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.neoforged.neoforge.client.buffer.param.state.WriteMaskState;

public record WriteMaskParam(WriteMaskState writeMaskState) implements IBufferDefinitionParam<WriteMaskState> {

    @Override
    public WriteMaskState getValue() {
        return writeMaskState;
    }
    public static final class Vanilla {
        public static final WriteMaskParam COLOR_DEPTH_WRITE = new WriteMaskParam(WriteMaskState.Vanilla.COLOR_DEPTH_WRITE);
        public static final WriteMaskParam COLOR_WRITE = new WriteMaskParam(WriteMaskState.Vanilla.COLOR_WRITE);
        public static final WriteMaskParam DEPTH_WRITE = new WriteMaskParam(WriteMaskState.Vanilla.DEPTH_WRITE);
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypeManager.WRITE_MASK;
    }
}
