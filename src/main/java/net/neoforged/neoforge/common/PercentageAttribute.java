/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.TooltipFlag;

/**
 * A Percentage Attribute is one which always displays modifiers as percentages, including for {@link Operation#ADD_VALUE}.
 * <p>
 * This is used for attributes that would not make sense being displayed as flat additions (ex: +0.05 Swim Speed).
 */
public class PercentageAttribute extends RangedAttribute {
    public PercentageAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax) {
        super(pDescriptionId, pDefaultValue, pMin, pMax);
    }

    @Override
    public MutableComponent toValueComponent(Operation op, double value, TooltipFlag flag) {
        return Component.translatable("neoforge.value.percent", FORMAT.format(value * 100));
    }
}
