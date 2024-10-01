/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

public record FormatParam(VertexFormat vertexFormat) implements IBufferDefinitionParam<VertexFormat> {

    @Override
    public VertexFormat getValue() {
        return vertexFormat;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypes.FORMAT;
    }
    public static final class Vanilla {
        public static final FormatParam BLIT_SCREEN = new FormatParam(DefaultVertexFormat.BLIT_SCREEN);
        public static final FormatParam BLOCK = new FormatParam(DefaultVertexFormat.BLOCK);
        public static final FormatParam NEW_ENTITY = new FormatParam(DefaultVertexFormat.NEW_ENTITY);
        public static final FormatParam PARTICLE = new FormatParam(DefaultVertexFormat.PARTICLE);
        public static final FormatParam POSITION = new FormatParam(DefaultVertexFormat.POSITION);
        public static final FormatParam POSITION_COLOR = new FormatParam(DefaultVertexFormat.POSITION_COLOR);
        public static final FormatParam POSITION_COLOR_NORMAL = new FormatParam(DefaultVertexFormat.POSITION_COLOR_NORMAL);
        public static final FormatParam POSITION_COLOR_LIGHTMAP = new FormatParam(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP);
        public static final FormatParam POSITION_TEX = new FormatParam(DefaultVertexFormat.POSITION_TEX);
        public static final FormatParam POSITION_TEX_COLOR = new FormatParam(DefaultVertexFormat.POSITION_TEX_COLOR);
        public static final FormatParam POSITION_COLOR_TEX_LIGHTMAP = new FormatParam(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        public static final FormatParam POSITION_TEX_LIGHTMAP_COLOR = new FormatParam(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
        public static final FormatParam POSITION_TEX_COLOR_NORMAL = new FormatParam(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
    }
}
