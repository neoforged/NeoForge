/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.recipebook;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class RecipeBookTestRecipe implements Recipe<CraftingInput> {
    public final Ingredients ingredients;
    private final int width;
    private final int height;
    private final NonNullList<Ingredient> items;

    public RecipeBookTestRecipe(Ingredients ingredients) {
        this.ingredients = ingredients;
        this.width = ingredients.pattern.get(0).length();
        this.height = ingredients.pattern.size();
        List<String> pattern = new ArrayList<>(ingredients.pattern); //might need to reverse this list.
        while (pattern.size() != 4)
            pattern.add("  ");
        this.items = pattern.stream()
                .flatMap(s -> Stream.of(s.substring(0, 1), s.substring(1, 2)))
                .map(s -> s.equals(" ") ? Ingredient.EMPTY : ingredients.recipe.get(s))
                .peek(i -> Objects.requireNonNull(i, "A key in sculpting pattern was not defined!"))
                .collect(Collectors.toCollection(NonNullList::create));
    }

    /**
     * Taken from {@link ShapedRecipe}
     */
    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() == this.width && input.height() == this.height) {
            if (this.matches(input, true) || this.matches(input, false))
                return true;
        }

        return false;
    }

    private boolean matches(CraftingInput input, boolean mirror) //unsure about the last boolean
    {
        for (int x = 0; x < 2; ++x) {
            for (int y = 0; y < 4; ++y) {
                Ingredient ingredient = Ingredient.EMPTY;
                int idx = mirror ? this.width - x - 1 + y * this.width : x + y * this.width;
                ingredient = this.items.get(idx);

                if (!ingredient.test(input.getItem(x + y * 2)))
                    return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput p_44001_, HolderLookup.Provider registryAccess) {
        return this.getResultItem(registryAccess).copy();
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return this.width <= p_43999_ && this.height <= p_44000_; //used for recipe book
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return this.ingredients.result();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeBookExtensionTest.RECIPE_BOOK_TEST_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeBookExtensionTest.RECIPE_BOOK_TEST_RECIPE_TYPE.get();
    }

    @Override
    public String getGroup() {
        return this.ingredients.group;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.items;
    }

    @Override
    public boolean isIncomplete() {
        return this.getIngredients().isEmpty() ||
                this.getIngredients().stream()
                        .filter((ingredient) -> !ingredient.isEmpty())
                        .anyMatch(Ingredient::hasNoItems);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Items.NETHERITE_BLOCK);
    }

    public record Ingredients(String group, List<String> pattern, Map<String, Ingredient> recipe, ItemStack result) {
        private static final Function<String, DataResult<String>> VERIFY_LENGTH_2 = s -> s.length() == 2 ? DataResult.success(s) : DataResult.error(() -> "Key row length must be of 2!");
        private static final Function<List<String>, DataResult<List<String>>> VERIFY_SIZE = l -> {
            if (l.size() <= 4 && l.size() >= 1) {
                List<String> temp = new ArrayList<>(l);
                Collections.reverse(temp); //reverse so the first row is at the bottom in the json.
                return DataResult.success(ImmutableList.copyOf(temp));
            }
            return DataResult.error(() -> "Pattern must have between 1 and 4 rows of keys");
        };
        private static final Function<String, DataResult<String>> VERIFY_LENGTH_1 = s -> s.length() == 1 ? DataResult.success(s) : DataResult.error(() -> "Key must be a single character!");

        public static final MapCodec<Ingredients> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.fieldOf("group").forGetter(Ingredients::group),
                Codec.STRING.flatXmap(VERIFY_LENGTH_2, VERIFY_LENGTH_2).listOf().flatXmap(VERIFY_SIZE, VERIFY_SIZE).fieldOf("pattern").forGetter(Ingredients::pattern),
                Codec.unboundedMap(Codec.STRING.flatXmap(VERIFY_LENGTH_1, VERIFY_LENGTH_1), Ingredient.CODEC).fieldOf("key").forGetter(Ingredients::recipe),
                ItemStack.CODEC.fieldOf("result").forGetter(Ingredients::result)).apply(inst, Ingredients::new));
    }
}
