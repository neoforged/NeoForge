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
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;

public class RecipeBookTestComponent extends RecipeBookComponent<RecipeBookTestMenu> {
    private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("recipe_book/filter_enabled"),
            Identifier.withDefaultNamespace("recipe_book/filter_disabled"),
            Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
            Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted"));
    private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
            new RecipeBookComponent.TabInfo(new ItemStack(Items.COMPASS), Optional.empty(), RecipeBookExtensionTest.SEARCH_CATEGORY),
            new RecipeBookComponent.TabInfo(Items.DIAMOND, RecipeBookExtensionTest.RECIPE_BOOK_TEST_CAT1.get()));

    public RecipeBookTestComponent(RecipeBookTestMenu menu) {
        super(menu, TABS);
    }

    @Override
    protected WidgetSprites getFilterButtonTextures() {
        return FILTER_BUTTON_SPRITES;
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        int i = slot.index;
        return i == RecipeBookTestMenu.RESULT_SLOT || (RecipeBookTestMenu.CRAFTING_START <= i && i <= RecipeBookTestMenu.CRAFTING_STOP);
    }

    @Override
    protected void selectMatchingRecipes(RecipeCollection collection, StackedItemContents stackedContents) {
        collection.selectRecipes(stackedContents, display -> true);
    }

    @Override
    protected Component getRecipeFilterName() {
        return Component.literal("Only Craftables Tooltip");
    }

    @Override
    protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay display, ContextMap context) {
        ghostSlots.setResult(this.menu.resultSlot, context, display.result());
        if (display instanceof ShapedCraftingRecipeDisplay shapedcraftingrecipedisplay) {
            List<Slot> list1 = this.menu.getGridSlots();
            PlaceRecipeHelper.placeRecipe(
                    4,
                    2,
                    shapedcraftingrecipedisplay.width(),
                    shapedcraftingrecipedisplay.height(),
                    shapedcraftingrecipedisplay.ingredients(),
                    (ingredient, gridIndex, gridXPos, gridYPos) -> {
                        Slot slot = list1.get(gridIndex);
                        ghostSlots.setInput(slot, context, ingredient);
                    });
        }
    }
}
