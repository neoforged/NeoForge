/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.minecraft.client.renderer.RenderType;

public record OutlineParam(RenderType.OutlineProperty outlineProperty) implements IBufferDefinitionParam<RenderType.OutlineProperty> {

    @Override
    public RenderType.OutlineProperty getValue() {
        return outlineProperty;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypes.OUTLINE;
    }
    public static final class Vanilla {
        public static final OutlineParam NONE = new OutlineParam(RenderType.OutlineProperty.NONE);
        public static final OutlineParam IS_OUTLINE = new OutlineParam(RenderType.OutlineProperty.IS_OUTLINE);
        public static final OutlineParam AFFECTS_OUTLINE = new OutlineParam(RenderType.OutlineProperty.AFFECTS_OUTLINE);
    }
}
