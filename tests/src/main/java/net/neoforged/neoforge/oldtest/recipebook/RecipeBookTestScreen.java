/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.recipebook;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Mostly copied from {@link CraftingScreen}
 */
public class RecipeBookTestScreen extends AbstractRecipeBookScreen<RecipeBookTestMenu> implements RecipeUpdateListener {
    private static final ResourceLocation TEXTURE = RecipeBookExtensionTest.getId("textures/gui/container/recipe_book_test.png");

    public RecipeBookTestScreen(RecipeBookTestMenu menu, Inventory inv, Component title) {
        super(menu, new RecipeBookTestComponent(menu), inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = this.imageWidth - this.font.width(Language.getInstance().getVisualOrder(this.title)) - 5;
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
