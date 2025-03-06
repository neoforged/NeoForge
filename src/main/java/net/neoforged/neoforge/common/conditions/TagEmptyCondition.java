/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public record TagEmptyCondition<T>(TagKey<T> tag) implements ICondition {
    public static final MapCodec<TagEmptyCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(ResourceLocation.CODEC.optionalFieldOf("registry", Registries.ITEM.location()).forGetter(condition -> condition.tag().registry().location()),
                    ResourceLocation.CODEC.fieldOf("tag").forGetter(condition -> condition.tag().location()))
            .apply(instance, TagEmptyCondition::new));

    private TagEmptyCondition(ResourceLocation registryType, ResourceLocation tagName) {
        this(TagKey.create(ResourceKey.createRegistryKey(registryType), tagName));
    }

    @Override
    public boolean test(ICondition.IContext context) {
        return !context.isTagLoaded(tag);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
