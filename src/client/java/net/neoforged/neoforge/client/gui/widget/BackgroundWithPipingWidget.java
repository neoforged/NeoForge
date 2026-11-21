/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// Renders the background and top/bottom piping from [net.minecraft.client.gui.components.AbstractSelectionList].
public class BackgroundWithPipingWidget extends AbstractWidget {
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");

    @Nullable
    private final Minecraft minecraft;

    public BackgroundWithPipingWidget(@Nullable Minecraft minecraft, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.minecraft = minecraft;
    }

    public BackgroundWithPipingWidget(int x, int y, int width, int height) {
        this(null, x, y, width, height);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false; // Avoid capturing mouse events
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Identifier headerSeparator = this.minecraft == null || this.minecraft.level == null ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        Identifier footerSeparator = this.minecraft == null || this.minecraft.level == null ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        graphics.blit(RenderPipelines.GUI_TEXTURED, headerSeparator, this.getX(), this.getY() - 2, 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, footerSeparator, this.getX(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2);

        Identifier menuListBackground = this.minecraft == null || this.minecraft.level == null ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                menuListBackground,
                this.getX(),
                this.getY(),
                (float) this.getRight(),
                (float) this.getBottom(),
                this.getWidth(),
                this.getHeight(),
                32,
                32);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        // No-op
    }

    @Override
    public void playDownSound(SoundManager soundManager) {}

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        return null;
    }
}
