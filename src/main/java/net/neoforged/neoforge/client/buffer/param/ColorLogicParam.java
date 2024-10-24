/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Optional;

public record ColorLogicParam(Optional<GlStateManager.LogicOp> logicOp) implements IBufferDefinitionParam<Optional<GlStateManager.LogicOp>> {

    public ColorLogicParam(GlStateManager.LogicOp logicOp) {
        this(Optional.of(logicOp));
    }

    public ColorLogicParam() {
        this(Optional.empty());
    }

    @Override
    public Optional<GlStateManager.LogicOp> getValue() {
        return logicOp;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypeManager.COLOR_LOGIC;
    }
    public static final class Vanilla {
        public static final ColorLogicParam NO_COLOR_LOGIC = new ColorLogicParam(Optional.empty());
        public static final ColorLogicParam OR_REVERSE_COLOR_LOGIC = new ColorLogicParam(GlStateManager.LogicOp.OR_REVERSE);
    }
}
