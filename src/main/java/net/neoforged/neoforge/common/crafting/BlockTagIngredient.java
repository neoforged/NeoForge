/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Ingredient that matches ItemStacks of blocks from a TagKey<Block>, useful in cases
 * like "minecraft:convertable_to_mud" where there isn't an accompanying item tag
 * <p>
 * Notice: This should not be used as a replacement for the normal Item Tag Ingredient,
 * This should only be used when there is no way an item tag can be used in your use case
 */
public class BlockTagIngredient extends Ingredient {
    public static final Codec<BlockTagIngredient> CODEC = RecordCodecBuilder.create(
            valueInstance -> valueInstance.group(TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(BlockTagIngredient::getTag))
                    .apply(valueInstance, BlockTagIngredient::new));

    protected final TagKey<Block> tag;

    @Nullable
    protected ItemStack[] itemStacks;

    public BlockTagIngredient(TagKey<Block> tag) {
        super(Stream.of(new BlockTagValue(tag)), NeoForgeMod.BLOCK_TAG_INGREDIENT);
        this.tag = tag;
    }

    protected void dissolve() {
        if (itemStacks == null) {
            List<ItemStack> list = new ArrayList<>();
            for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                ItemStack stack = new ItemStack(block.value());
                if (!stack.isEmpty()) {
                    list.add(stack);
                }
            }
            itemStacks = list.toArray(ItemStack[]::new);
        }
    }

    @Override
    public ItemStack[] getItems() {
        dissolve();
        return itemStacks;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }

        dissolve();
        if (itemStacks.length == 0) {
            return stack.isEmpty();
        }

        for (ItemStack itemStack : itemStacks) {
            if (itemStack.is(stack.getItem())) {
                return true;
            }
        }

        return false;
    }

    public TagKey<Block> getTag() {
        return tag;
    }

    @Override
    public IngredientType<?> getType() {
        return NeoForgeMod.BLOCK_TAG_INGREDIENT.get();
    }

    public record BlockTagValue(TagKey<Block> tag) implements Ingredient.Value {
        @Override
        public boolean equals(Object pOther) {
            return pOther instanceof BlockTagValue tagValue && tagValue.tag.location().equals(this.tag.location());
        }

        @Override
        public Collection<ItemStack> getItems() {
            List<ItemStack> list = new ArrayList<>();

            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(this.tag)) {
                list.add(new ItemStack(holder.value().asItem()));
            }

            if (list.size() == 0) {
                list.add(new ItemStack(Blocks.BARRIER).setHoverName(Component.literal("Empty Tag: " + this.tag.location())));
            }

            return list;
        }
    }
}
