/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record TagEmptyCondition(TagKey<Item> tag) implements ICondition
{
    public static final Codec<TagEmptyCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          ResourceLocation.CODEC.xmap(loc -> TagKey.create(Registries.ITEM, loc), TagKey::location).fieldOf("tag").forGetter(TagEmptyCondition::tag)
    ).apply(builder, TagEmptyCondition::new));

    public TagEmptyCondition(String location)
    {
        this(new ResourceLocation(location));
    }

    public TagEmptyCondition(String namespace, String path)
    {
        this(new ResourceLocation(namespace, path));
    }

    public TagEmptyCondition(ResourceLocation tag)
    {
        this(TagKey.create(Registries.ITEM, tag));
    }

    @Override
    public boolean test(ICondition.IContext context)
    {
        return context.getTag(tag).isEmpty();
    }
    
    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }
    
    @Override
    public String toString()
    {
        return "tag_empty(\"" + tag.location() + "\")";
    }
}
