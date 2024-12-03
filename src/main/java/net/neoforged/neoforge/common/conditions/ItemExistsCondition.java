/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public record ItemExistsCondition(ResourceLocation item) implements ICondition {
    public static MapCodec<ItemExistsCondition> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            ResourceLocation.CODEC.fieldOf("item").forGetter(ItemExistsCondition::item))
                    .apply(builder, ItemExistsCondition::new));

    @Override
    public boolean test(IContext context) {
        return BuiltInRegistries.ITEM.containsKey(item);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "item_exists(\"" + item + "\")";
    }
}
