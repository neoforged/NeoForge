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
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Ingredient} that matches {@link ItemStack}s of {@link Block}s from a {@link TagKey<Block>}, useful in cases
 * like {@code "minecraft:convertable_to_mud"} where there isn't an accompanying item tag
 * <p>
 * Notice: This should not be used as a replacement for the normal item tag ingredient.
 * This should only be used when there is no way an item tag can be used in your use case
 */
public class BlockTagIngredient implements ICustomIngredient {
    public static final MapCodec<BlockTagIngredient> CODEC = TagKey.codec(Registries.BLOCK)
            .xmap(BlockTagIngredient::new, BlockTagIngredient::getTag).fieldOf("tag");

    protected final TagKey<Block> tag;

    @Nullable
    protected HolderSet<Item> items;

    public BlockTagIngredient(TagKey<Block> tag) {
        this.tag = tag;
    }

    protected HolderSet<Item> dissolve() {
        if (items == null) {
            List<Holder<Item>> list = new ArrayList<>();
            for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                var item = block.value().asItem();
                if (item != Items.AIR) {
                    list.add(item.builtInRegistryHolder());
                }
            }

            items = HolderSet.direct(list);
        }
        return items;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return dissolve().stream();
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null)
            return false;

        return dissolve().contains(stack.getItemHolder());
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
    public SlotDisplay display() {
        return new SlotDisplay.Composite(dissolve().stream()
                .map(Ingredient::displayForSingleItem)
                .toList());
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
