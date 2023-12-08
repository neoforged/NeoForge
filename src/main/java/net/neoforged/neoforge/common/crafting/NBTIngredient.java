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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/**
 * Ingredient that matches the given items, performing either a {@link NBTIngredient#isStrict() strict} or a partial NBT test.
 * <p>
 * Strict NBT ingredients will only match items that have <b>exactly</b> the provided tag, while partial ones will
 * match if the item's tags contain all of the elements of the provided one, while allowing for additional elements to exist.
 */
public class NBTIngredient extends Ingredient {

    public static final Codec<NBTIngredient> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            NeoForgeExtraCodecs.singularOrPluralCodec(BuiltInRegistries.ITEM.byNameCodec(), "item").forGetter(NBTIngredient::getContainedItems),
                            CraftingHelper.TAG_CODEC.fieldOf("tag").forGetter(NBTIngredient::getTag),
                            Codec.BOOL.optionalFieldOf("strict", false).forGetter(NBTIngredient::isStrict))
                    .apply(builder, NBTIngredient::new));

    public static final Codec<NBTIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            NeoForgeExtraCodecs.singularOrPluralCodecNotEmpty(BuiltInRegistries.ITEM.byNameCodec(), "item").forGetter(NBTIngredient::getContainedItems),
                            CraftingHelper.TAG_CODEC.fieldOf("tag").forGetter(NBTIngredient::getTag),
                            Codec.BOOL.optionalFieldOf("strict", false).forGetter(NBTIngredient::isStrict))
                    .apply(builder, NBTIngredient::new));

    private final boolean strict;

    protected NBTIngredient(Set<Item> items, CompoundTag tag, boolean strict) {
        super(items.stream().map(item -> {
            ItemStack stack = new ItemStack(item, 1);
            // copy NBT to prevent the stack from modifying the original, as attachments or vanilla item durability will modify the tag
            stack.setTag(tag.copy());
            return new Ingredient.ItemValue(stack, strict ? ItemStack::matches : NBTIngredient::compareStacksWithNBT);
        }), NeoForgeMod.NBT_INGREDIENT_TYPE);

        if (items.isEmpty())
            throw new IllegalStateException("At least one item needs to be provided for a nbt matching ingredient.");

        this.strict = strict;
    }

    @Override
    protected boolean areStacksEqual(ItemStack left, ItemStack right) {
        return strict ? ItemStack.matches(left, right) : compareStacksWithNBT(left, right);
    }

    @Override
    public boolean synchronizeWithContents() {
        return false;
    }

    private static boolean compareStacksWithNBT(ItemStack left, ItemStack right) {
        return left.getItem() == right.getItem() && NbtUtils.compareNbt(left.getTag(), right.getTag(), true);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given NBT
     */
    public static NBTIngredient of(boolean strict, CompoundTag nbt, ItemLike... items) {
        return new NBTIngredient(Arrays.stream(items).map(ItemLike::asItem).collect(Collectors.toSet()), nbt, strict);
    }

    /**
     * Creates a new ingredient matching the given item, containing the given NBT
     */
    public static NBTIngredient of(ItemLike item, CompoundTag nbt, boolean strict) {
        return new NBTIngredient(Set.of(item.asItem()), nbt, strict);
    }

    /**
     * Creates a new ingredient matching the given item, containing the given NBT
     */
    public static NBTIngredient of(ItemStack stack, boolean strict) {
        return new NBTIngredient(Set.of(stack.getItem()), stack.getOrCreateTag(), strict);
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

    @Override
    public boolean isSimple() {
        return false;
    }

    public boolean isStrict() {
        return strict;
    }
}
