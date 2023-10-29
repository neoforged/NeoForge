/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.registries.ForgeRegistries;

/** Ingredient that matches the given items, performing a partial NBT match. Use {@link StrictNBTIngredient} if you want exact match on NBT */
public class PartialNBTIngredient extends Ingredient {

    public static final Codec<PartialNBTIngredient> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            NeoForgeExtraCodecs.singularOrPluralCodec(ForgeRegistries.ITEMS.getCodec(), "item").forGetter(PartialNBTIngredient::getContainedItems),
                            CompoundTag.CODEC.fieldOf("tag").forGetter(PartialNBTIngredient::getTag))
                    .apply(builder, PartialNBTIngredient::new));

    public static final Codec<PartialNBTIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            NeoForgeExtraCodecs.singularOrPluralCodecNotEmpty(ForgeRegistries.ITEMS.getCodec(), "item").forGetter(PartialNBTIngredient::getContainedItems),
                            CompoundTag.CODEC.fieldOf("tag").forGetter(PartialNBTIngredient::getTag))
                    .apply(builder, PartialNBTIngredient::new));

    protected PartialNBTIngredient(Set<Item> items, CompoundTag tag) {
        super(items.stream().map(item -> {
            ItemStack stack = new ItemStack(item, 1);
            // copy NBT to prevent the stack from modifying the original, as capabilities or vanilla item durability will modify the tag
            stack.setTag(tag.copy());
            return new Ingredient.ItemValue(stack, PartialNBTIngredient::compareStacksUsingPredicate);
        }), NeoForgeMod.PARTIAL_NBT_INGREDIENT_TYPE::get);

        if (items.isEmpty())
            throw new IllegalStateException("At least one item needs to be provided for a partial nbt matching ingredient.");
    }

    @Override
    protected boolean areStacksEqual(ItemStack left, ItemStack right) {
        return compareStacksUsingPredicate(left, right);
    }

    @Override
    public boolean synchronizeWithContents() {
        return false;
    }

    private static boolean compareStacksUsingPredicate(ItemStack left, ItemStack right) {
        NbtPredicate predicate = new NbtPredicate(left.getOrCreateTag());
        return left.getItem() == right.getItem() && predicate.matches(right);
    }

    /** Creates a new ingredient matching any item from the list, containing the given NBT */
    public static PartialNBTIngredient of(CompoundTag nbt, ItemLike... items) {
        return new PartialNBTIngredient(Arrays.stream(items).map(ItemLike::asItem).collect(Collectors.toSet()), nbt);
    }

    /** Creates a new ingredient matching the given item, containing the given NBT */
    public static PartialNBTIngredient of(ItemLike item, CompoundTag nbt) {
        return new PartialNBTIngredient(Set.of(item.asItem()), nbt);
    }

    public Set<Item> getContainedItems() {
        return Arrays.stream(getItems()).map(ItemStack::getItem).collect(Collectors.toSet());
    }

    public CompoundTag getTag() {
        final ItemStack[] items = getItems();
        if (items.length == 0)
            return new CompoundTag();

        return items[0].getOrCreateTag();
    }

    public boolean isSimple() {
        return false;
    }
}
