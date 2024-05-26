/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Ingredient} that matches {@link ItemStack}s of {@link Block}s from a {@link TagKey<Block>}, useful in cases
 * like {@code "minecraft:convertable_to_mud"} where there isn't an accompanying item tag
 * <p>
 * Notice: This should not be used as a replacement for the normal {@link Ingredient#of(TagKey)},
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

            if (list.isEmpty()) {
                ItemStack itemStack = new ItemStack(net.minecraft.world.level.block.Blocks.BARRIER);
                itemStack.set(DataComponents.CUSTOM_NAME, Component.literal("Empty Tag: " + this.tag.location()));
                list.add(itemStack);
            }

            itemStacks = list.toArray(ItemStack[]::new);
        }
    }

    @Override
    public Stream<ItemStack> getItems() {
        dissolve();
        return Stream.of(itemStacks);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null)
            return false;

        dissolve();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockTagIngredient that)) return false;
        return tag.equals(that.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }
}
