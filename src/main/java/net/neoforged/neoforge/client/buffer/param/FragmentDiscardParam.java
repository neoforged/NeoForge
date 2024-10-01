/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import java.util.OptionalDouble;

public record FragmentDiscardParam(OptionalDouble cutoff) implements IBufferDefinitionParam<OptionalDouble> {

    public FragmentDiscardParam(double cutoff) {
        this(OptionalDouble.of(cutoff));
    }

    public FragmentDiscardParam() {
        this(OptionalDouble.empty());
    }

    @Override
    public OptionalDouble getValue() {
        return cutoff;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypes.FRAGMENT_DISCARD;
    }
    public static final class Vanilla {
        public static final FragmentDiscardParam ONE = new FragmentDiscardParam(1.0);
        public static final FragmentDiscardParam HALF = new FragmentDiscardParam(0.5);
        public static final FragmentDiscardParam ONE_TENTH = new FragmentDiscardParam(0.1);
        public static final FragmentDiscardParam ZERO = new FragmentDiscardParam(0.0);
    }
}
