/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NotCondition(ICondition value) implements ICondition {
    public static final MapCodec<NotCondition> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            ICondition.CODEC.fieldOf("value").forGetter(NotCondition::value))
                    .apply(builder, NotCondition::new));

    @Override
    public boolean test(IContext context) {
        return !value.test(context);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "!" + value;
    }
}
