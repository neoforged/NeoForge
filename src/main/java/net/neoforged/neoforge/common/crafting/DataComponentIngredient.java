/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

/**
 * Ingredient that matches the given items, performing either a {@link DataComponentIngredient#isStrict() strict} or a partial NBT test.
 * <p>
 * Strict NBT ingredients will only match items that have <b>exactly</b> the provided tag, while partial ones will
 * match if the item's tags contain all of the elements of the provided one, while allowing for additional elements to exist.
 */
public class DataComponentIngredient extends Ingredient {
    public static final MapCodec<DataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("items").forGetter(DataComponentIngredient::items),
                            DataComponentPredicate.CODEC.fieldOf("components").forGetter(DataComponentIngredient::components),
                            Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentIngredient::isStrict))
                    .apply(builder, DataComponentIngredient::new));
    public static final MapCodec<DataComponentIngredient> CODEC_NONEMPTY = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("items").forGetter(DataComponentIngredient::items),
                            DataComponentPredicate.CODEC.fieldOf("components").forGetter(DataComponentIngredient::components),
                            Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentIngredient::isStrict))
                    .apply(builder, DataComponentIngredient::new));

    private final HolderSet<Item> items;
    private final DataComponentPredicate components;
    private final boolean strict;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected DataComponentIngredient(HolderSet<Item> items, DataComponentPredicate components, boolean strict) {
        super(items.stream().map(item -> {
            ItemStack stack = new ItemStack(item, 1, components.asPatch());
            return new Ingredient.ItemValue(stack, ItemStack::matches);
        }), NeoForgeMod.NBT_INGREDIENT_TYPE);
        this.items = items;
        this.components = components;
        this.strict = strict;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) return false;
        if (strict) {
            for (ItemStack stack2 : getItems()) {
                if (ItemStack.matches(stack, stack2)) return true;
            }
            return false;
        } else {
            return this.items.contains(stack.getItemHolder()) && this.components.test(stack);
        }
    }

    @Override
    public boolean synchronizeWithContents() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public HolderSet<Item> items() {
        return items;
    }

    public DataComponentPredicate components() {
        return components;
    }

    public boolean isStrict() {
        return strict;
    }

    /**
     * Creates a new ingredient matching the given item, containing the given components
     */
    public static DataComponentIngredient of(boolean strict, ItemStack stack) {
        return of(strict, stack.getComponents(), stack.getItem());
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static <T> DataComponentIngredient of(boolean strict, DataComponentType<? super T> type, T value, ItemLike... items) {
        return of(strict, DataComponentPredicate.builder().expect(type, value).build(), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static <T> DataComponentIngredient of(boolean strict, Supplier<? extends DataComponentType<? super T>> type, T value, ItemLike... items) {
        return of(strict, DataComponentPredicate.builder().expect(type, value).build(), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static DataComponentIngredient of(boolean strict, DataComponentMap map, ItemLike... items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    @SafeVarargs
    public static DataComponentIngredient of(boolean strict, DataComponentMap map, Holder<Item>... items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static DataComponentIngredient of(boolean strict, DataComponentMap map, HolderSet<Item> items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    @SafeVarargs
    public static DataComponentIngredient of(boolean strict, DataComponentPredicate predicate, Holder<Item>... items) {
        return of(strict, predicate, HolderSet.direct(items));
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static DataComponentIngredient of(boolean strict, DataComponentPredicate predicate, ItemLike... items) {
        return of(strict, predicate, HolderSet.direct(Arrays.stream(items).map(ItemLike::asItem).map(Item::builtInRegistryHolder).toList()));
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static DataComponentIngredient of(boolean strict, DataComponentPredicate predicate, HolderSet<Item> items) {
        return new DataComponentIngredient(items, predicate, strict);
    }
}
