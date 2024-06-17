/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.recipebook;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;

public class RecipeBookExtensionClientHelper {
    @SuppressWarnings("unused") // referenced by enumextender.json
    public static final EnumProxy<RecipeBookCategories> CAT_SEARCH_ENUM_PARAMS = new EnumProxy<>(
            RecipeBookCategories.class,
            (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Items.COMPASS)));
    @SuppressWarnings("unused") // referenced by enumextender.json
    public static final EnumProxy<RecipeBookCategories> CAT_1_ENUM_PARAMS = new EnumProxy<>(
            RecipeBookCategories.class,
            (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Items.DIAMOND)));
    @SuppressWarnings("unused") // referenced by enumextender.json
    public static final EnumProxy<RecipeBookCategories> CAT_2_ENUM_PARAMS = new EnumProxy<>(
            RecipeBookCategories.class,
            (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Items.NETHERITE_INGOT)));

    public static final Supplier<RecipeBookCategories> TESTING_SEARCH = Suppliers.memoize(CAT_SEARCH_ENUM_PARAMS::getValue);
    public static final Supplier<RecipeBookCategories> TESTING_CAT_1 = Suppliers.memoize(CAT_1_ENUM_PARAMS::getValue);
    public static final Supplier<RecipeBookCategories> TESTING_CAT_2 = Suppliers.memoize(CAT_2_ENUM_PARAMS::getValue);

    public static void init(RegisterRecipeBookCategoriesEvent event) {
        event.registerBookCategories(RecipeBookExtensionTest.TEST_TYPE, ImmutableList.of(TESTING_SEARCH.get(), TESTING_CAT_1.get(), TESTING_CAT_2.get()));
        event.registerAggregateCategory(TESTING_SEARCH.get(), ImmutableList.of(TESTING_CAT_1.get(), TESTING_CAT_2.get()));
        event.registerRecipeCategoryFinder(RecipeBookExtensionTest.RECIPE_BOOK_TEST_RECIPE_TYPE.get(), r -> {
            if (r.value().getResultItem(Minecraft.getInstance().level.registryAccess()).getItem() == Items.DIAMOND_BLOCK)
                return TESTING_CAT_1.get();
            else return TESTING_CAT_2.get();
        });
    }
}
