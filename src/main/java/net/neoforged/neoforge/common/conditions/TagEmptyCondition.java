/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record TagEmptyCondition(TagKey<Item> tag) implements ICondition {
    public static final MapCodec<TagEmptyCondition> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            ResourceLocation.CODEC.xmap(loc -> TagKey.create(Registries.ITEM, loc), TagKey::location).fieldOf("tag").forGetter(TagEmptyCondition::tag))
                    .apply(builder, TagEmptyCondition::new));

    public TagEmptyCondition(String location) {
        this(ResourceLocation.parse(location));
    }

    public TagEmptyCondition(String namespace, String path) {
        this(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public TagEmptyCondition(ResourceLocation tag) {
        this(TagKey.create(Registries.ITEM, tag));
    }

    @Override
    public boolean test(ICondition.IContext context) {
        return !context.isTagLoaded(tag);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "tag_empty(\"" + tag.location() + "\")";
    }
}
