/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.Codec;

public final class TrueCondition implements ICondition {

    public static final TrueCondition INSTANCE = new TrueCondition();

    public static Codec<TrueCondition> CODEC = Codec.unit(INSTANCE).stable();

    private TrueCondition() {}

    @Override
    public boolean test(IContext context) {
        return true;
    }

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "true";
    }
}
