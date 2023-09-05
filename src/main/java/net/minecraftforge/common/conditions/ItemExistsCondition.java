/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemExistsCondition implements ICondition
{
    
    public static Codec<ItemExistsCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          ResourceLocation.CODEC.fieldOf("item").forGetter(ItemExistsCondition::getItem)
    ).apply(builder, ItemExistsCondition::new));
    
    private final ResourceLocation item;

    public ItemExistsCondition(String location)
    {
        this(new ResourceLocation(location));
    }

    public ItemExistsCondition(String namespace, String path)
    {
        this(new ResourceLocation(namespace, path));
    }

    public ItemExistsCondition(ResourceLocation item)
    {
        this.item = item;
    }

    @Override
    public boolean test(IContext context)
    {
        return ForgeRegistries.ITEMS.containsKey(item);
    }
    
    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }
    
    public ResourceLocation getItem() {
        return item;
    }
    
    @Override
    public String toString()
    {
        return "item_exists(\"" + item + "\")";
    }
}
