/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public final class FalseCondition implements ICondition {

    public static final FalseCondition INSTANCE = new FalseCondition();

    public static final Codec<FalseCondition> CODEC = MapCodec.unit(INSTANCE).stable().codec();

    private FalseCondition() {}

    @Override
    public boolean test(IContext condition) {
        return false;
    }

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }

    public String toString() {
        return "false";
    }
}
