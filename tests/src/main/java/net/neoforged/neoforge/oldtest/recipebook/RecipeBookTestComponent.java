/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.recipebook;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;

public class RecipeBookTestComponent extends RecipeBookComponent<RecipeBookTestMenu> {
    private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled"),
            ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled"),
            ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
            ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled_highlighted"));
    private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
            new RecipeBookComponent.TabInfo(new ItemStack(Items.COMPASS), Optional.empty(), RecipeBookExtensionTest.SEARCH_CATEGORY),
            new RecipeBookComponent.TabInfo(Items.DIAMOND, RecipeBookExtensionTest.RECIPE_BOOK_TEST_CAT1.get()));

    public RecipeBookTestComponent(RecipeBookTestMenu p_365070_) {
        super(p_365070_, TABS);
    }

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(FILTER_BUTTON_SPRITES);
    }

    @Override
    protected boolean isCraftingSlot(Slot p_361241_) {
        int i = p_361241_.index;
        return i == RecipeBookTestMenu.RESULT_SLOT || (RecipeBookTestMenu.CRAFTING_START <= i && i <= RecipeBookTestMenu.CRAFTING_STOP);
    }

    @Override
    protected void selectMatchingRecipes(RecipeCollection p_360862_, StackedItemContents p_363036_) {
        p_360862_.selectRecipes(p_363036_, display -> true);
    }

    @Override
    protected Component getRecipeFilterName() {
        return Component.literal("Only Craftables Tooltip");
    }

    @Override
    protected void fillGhostRecipe(GhostSlots p_380075_, RecipeDisplay display, ContextMap p_381016_) {
        p_380075_.setResult(this.menu.resultSlot, p_381016_, display.result());
        if (display instanceof ShapedCraftingRecipeDisplay shapedcraftingrecipedisplay) {
            List<Slot> list1 = this.menu.getGridSlots();
            PlaceRecipeHelper.placeRecipe(
                    4,
                    2,
                    shapedcraftingrecipedisplay.width(),
                    shapedcraftingrecipedisplay.height(),
                    shapedcraftingrecipedisplay.ingredients(),
                    (p_380786_, p_380787_, p_380788_, p_380789_) -> {
                        Slot slot = list1.get(p_380787_);
                        p_380075_.setInput(slot, p_381016_, p_380786_);
                    });
        }
    }
}
