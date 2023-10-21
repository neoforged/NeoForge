/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.google.common.base.Joiner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record AndCondition(List<ICondition> children) implements ICondition
{
    
    public static final Codec<AndCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          LIST_CODEC.fieldOf("values").forGetter(AndCondition::children)
    ).apply(builder, AndCondition::new));
    
    @Override
    public boolean test(IContext context)
    {
        for (ICondition child : children)
        {
            if (!child.test(context))
                return false;
        }
        return true;
    }
    
    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }
    
    @Override
    public String toString()
    {
        return Joiner.on(" && ").join(children);
    }
}
