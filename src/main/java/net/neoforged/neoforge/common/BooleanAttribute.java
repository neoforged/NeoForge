/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * A boolean attribute only has two states, on or off, represented by a value of 0 (false) or 1 (true).
 * <p>
 * For boolean attributes, only use the following modifier values:
 * <ul>
 * <li>A value of 1 with {@link AttributeModifier.Operation#ADD_VALUE} to enable the effect.</li>
 * <li>A value of -1 with {@link AttributeModifier.Operation#ADD_MULTIPLIED_TOTAL} to forcibly disable the effect.</li>
 * </ul>
 * This behavior allows for multiple enabling modifiers to coexist, not removing the effect unless all enabling modifiers are removed.
 * <p>
 * Additionally, it permits forcibly disabling the effect through multiply total.
 * 
 * @apiNote Use of other operations and/or values will trigger undefined behavior, where no guarantees can be made if the attribute will be enabled or not.
 */
public class BooleanAttribute extends Attribute {
    public BooleanAttribute(String descriptionId, boolean defaultValue) {
        super(descriptionId, defaultValue ? 1 : 0);
    }

    @Override
    public double sanitizeValue(double value) {
        if (Double.isNaN(value)) {
            return 0;
        }
        return value > 0 ? 1 : 0;
    }
}
