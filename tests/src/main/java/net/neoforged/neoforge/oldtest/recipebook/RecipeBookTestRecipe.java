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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RecipeBookTestRecipe implements Recipe<CraftingInput> {
    public final Ingredients ingredients;
    private final int width;
    private final int height;
    private final List<Optional<Ingredient>> items;
    @Nullable
    private PlacementInfo placementInfo;

    public RecipeBookTestRecipe(Ingredients ingredients) {
        this.ingredients = ingredients;
        this.width = ingredients.pattern.get(0).length();
        this.height = ingredients.pattern.size();
        this.items = ingredients.pattern.stream()
                .flatMap(s -> Stream.of(s.substring(0, 1), s.substring(1, 2)))
                .map(s -> {
                    if (s.equals(" ")) {
                        return Optional.<Ingredient>empty();
                    } else {
                        var ing = ingredients.recipe.get(s);
                        Objects.requireNonNull(ing, "A key in sculpting pattern was not defined!");
                        return Optional.of(ing);
                    }
                })
                .toList();
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
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                int idx = mirror ? this.width - x - 1 + y * this.width : x + y * this.width;
                var ingredient = this.items.get(idx);

                if (!Ingredient.testOptionalIngredient(ingredient, input.getItem(x + y * this.width)))
                    return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput p_44001_, HolderLookup.Provider registryAccess) {
        return this.ingredients.result.copy();
    }

    @Override
    public RecipeSerializer<RecipeBookTestRecipe> getSerializer() {
        return RecipeBookExtensionTest.RECIPE_BOOK_TEST_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<RecipeBookTestRecipe> getType() {
        return RecipeBookExtensionTest.RECIPE_BOOK_TEST_RECIPE_TYPE.get();
    }

    @Override
    public String group() {
        return this.ingredients.group;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(this.items);
        }
        return this.placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new ShapedCraftingRecipeDisplay(
                        width,
                        height,
                        items.stream().map(p_380107_ -> p_380107_.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(),
                        new SlotDisplay.ItemStackSlotDisplay(ingredients.result),
                        new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        if (this.ingredients.result.is(Items.DIAMOND_BLOCK)) {
            return RecipeBookExtensionTest.RECIPE_BOOK_TEST_CAT1.get();
        } else {
            return RecipeBookExtensionTest.RECIPE_BOOK_TEST_CAT2.get();
        }
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
