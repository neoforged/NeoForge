/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;

public final class NeverCondition implements ICondition {
    public static final NeverCondition INSTANCE = new NeverCondition();

    public static final MapCodec<NeverCondition> CODEC = MapCodec.unit(INSTANCE).stable();

    private NeverCondition() {}

    @Override
    public boolean test(IContext condition) {
        return false;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public String toString() {
        return "never";
    }
}
