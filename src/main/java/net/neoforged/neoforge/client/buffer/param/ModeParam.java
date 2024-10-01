/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import com.mojang.blaze3d.vertex.VertexFormat;

public record ModeParam(VertexFormat.Mode mode) implements IBufferDefinitionParam<VertexFormat.Mode> {

    @Override
    public VertexFormat.Mode getValue() {
        return mode;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypes.MODE;
    }
    public static final class Vanilla {
        public static final ModeParam LINES = new ModeParam(VertexFormat.Mode.LINES);
        public static final ModeParam LINE_STRIP = new ModeParam(VertexFormat.Mode.LINE_STRIP);
        public static final ModeParam DEBUG_LINES = new ModeParam(VertexFormat.Mode.DEBUG_LINES);
        public static final ModeParam DEBUG_LINE_STRIP = new ModeParam(VertexFormat.Mode.DEBUG_LINE_STRIP);
        public static final ModeParam TRIANGLES = new ModeParam(VertexFormat.Mode.TRIANGLES);
        public static final ModeParam TRIANGLE_STRIP = new ModeParam(VertexFormat.Mode.TRIANGLE_STRIP);
        public static final ModeParam TRIANGLE_FAN = new ModeParam(VertexFormat.Mode.TRIANGLE_FAN);
        public static final ModeParam QUADS = new ModeParam(VertexFormat.Mode.QUADS);
    }
}
