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
import net.neoforged.neoforge.common.extensions.IAttributeExtension;

/**
 * A Percentage Attribute is one which always displays modifiers as percentages, including for {@link Operation#ADD_VALUE}.
 * <p>
 * This is used for attributes that would not make sense being displayed as flat additions (ex: +0.05 Swim Speed).
 */
public class PercentageAttribute extends RangedAttribute {
    protected final double scaleFactor;

    /**
     * Creates a new PercentageAttribute with the given description, value information, and scale factor.
     * <p>
     * If your attribute's "real" value correlates 1 == 100%, you would use a scale factor of 100 to convert to 1 to 100%.
     * 
     * @param pDescriptionId The description id used for generating the attribute's lang key.
     * @param pDefaultValue  The default value of the attribute
     * @param pMin           The minimum value
     * @param pMax           The maximum value
     * @param scaleFactor    The scale factor, used to convert the literal value to a percentage value.
     */
    public PercentageAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax, double scaleFactor) {
        super(pDescriptionId, pDefaultValue, pMin, pMax);
        this.scaleFactor = scaleFactor;
    }

    /**
     * Creates a new PercentageAttribute with the default scale factor of 100.
     * 
     * @see #PercentageAttribute(String, double, double, double, double)
     */
    public PercentageAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax) {
        this(pDescriptionId, pDefaultValue, pMin, pMax, 100);
    }

    @Override
    public MutableComponent toValueComponent(Operation op, double value, TooltipFlag flag) {
        if (IAttributeExtension.isNullOrAddition(op)) {
            return Component.translatable("neoforge.value.percent", FORMAT.format(value * this.scaleFactor));
        }

        return Component.translatable("neoforge.value.percent", FORMAT.format(value * 100));
    }
}
