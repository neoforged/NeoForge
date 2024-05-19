/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Ingredient that matches ItemStacks of blocks from a TagKey<Block>, useful in cases
 * like "minecraft:convertable_to_mud" where there isn't an accompanying item tag
 * <p>
 * Notice: This should not be used as a replacement for the normal Item Tag Ingredient,
 * This should only be used when there is no way an item tag can be used in your use case
 */
public class BlockTagIngredient implements ICustomIngredient {
    public static final MapCodec<BlockTagIngredient> CODEC = TagKey.codec(Registries.BLOCK)
            .xmap(BlockTagIngredient::new, BlockTagIngredient::getTag).fieldOf("tag");

    protected final TagKey<Block> tag;

    @Nullable
    protected ItemStack[] itemStacks;

    public BlockTagIngredient(TagKey<Block> tag) {
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
    public Stream<ItemStack> getItems() {
        dissolve();
        return itemStacks != null ? Stream.of(itemStacks) : Stream.empty();
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
    public boolean isSimple() {
        return true;
    }

    @Override
    public IngredientType<?> getType() {
        return NeoForgeMod.BLOCK_TAG_INGREDIENT.get();
    }
}
