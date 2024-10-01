/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import java.util.Optional;
import net.neoforged.neoforge.client.buffer.param.state.OutputState;

public record OutputParam(Optional<OutputState> renderTargetSupplier) implements IBufferDefinitionParam<Optional<OutputState>> {

    public OutputParam(OutputState outputState) {
        this(Optional.of(outputState));
    }

    public OutputParam() {
        this(Optional.empty());
    }

    @Override
    public Optional<OutputState> getValue() {
        return renderTargetSupplier;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypes.OUTPUT;
    }
    public static final class Vanilla {
        public static final OutputParam MAIN_TARGET = new OutputParam();
        public static final OutputParam OUTLINE_TARGET = new OutputParam(OutputState.Vanilla.OUTLINE_TARGET);
        public static final OutputParam TRANSLUCENT_TARGET = new OutputParam(OutputState.Vanilla.TRANSLUCENT_TARGET);
        public static final OutputParam PARTICLES_TARGET = new OutputParam(OutputState.Vanilla.PARTICLES_TARGET);
        public static final OutputParam WEATHER_TARGET = new OutputParam(OutputState.Vanilla.WEATHER_TARGET);
        public static final OutputParam CLOUDS_TARGET = new OutputParam(OutputState.Vanilla.CLOUDS_TARGET);
        public static final OutputParam ITEM_ENTITY_TARGET = new OutputParam(OutputState.Vanilla.ITEM_ENTITY_TARGET);
    }
}
