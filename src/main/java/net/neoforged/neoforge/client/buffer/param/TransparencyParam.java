/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import java.util.Optional;
import net.neoforged.neoforge.client.buffer.param.state.TransparencyState;

public record TransparencyParam(Optional<TransparencyState> transparencyState) implements IBufferDefinitionParam<Optional<TransparencyState>> {

    public TransparencyParam(TransparencyState transparencyState) {
        this(Optional.of(transparencyState));
    }

    public TransparencyParam() {
        this(Optional.empty());
    }

    @Override
    public Optional<TransparencyState> getValue() {
        return transparencyState;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypeManager.TRANSPARENCY;
    }
    public static final class Vanilla {
        public static final TransparencyParam NO_TRANSPARENCY = new TransparencyParam();
        public static final TransparencyParam ADDITIVE_TRANSPARENCY = new TransparencyParam(TransparencyState.Vanilla.ADDITIVE_TRANSPARENCY);
        public static final TransparencyParam LIGHTNING_TRANSPARENCY = new TransparencyParam(TransparencyState.Vanilla.LIGHTNING_TRANSPARENCY);
        public static final TransparencyParam GLINT_TRANSPARENCY = new TransparencyParam(TransparencyState.Vanilla.GLINT_TRANSPARENCY);
        public static final TransparencyParam CRUMBLING_TRANSPARENCY = new TransparencyParam(TransparencyState.Vanilla.CRUMBLING_TRANSPARENCY);
        public static final TransparencyParam TRANSLUCENT_TRANSPARENCY = new TransparencyParam(TransparencyState.Vanilla.TRANSLUCENT_TRANSPARENCY);
    }
}
