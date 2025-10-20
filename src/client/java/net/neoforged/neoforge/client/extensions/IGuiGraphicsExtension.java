/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Extension interface for {@link GuiGraphics}.
 */
public interface IGuiGraphicsExtension {
    private GuiGraphics self() {
        return (GuiGraphics) this;
    }

    int DEFAULT_BACKGROUND_COLOR = 0xF0100010;
    int DEFAULT_BORDER_COLOR_START = 0x505000FF;
    int DEFAULT_BORDER_COLOR_END = (DEFAULT_BORDER_COLOR_START & 0xFEFEFE) >> 1 | DEFAULT_BORDER_COLOR_START & 0xFF000000;
    String UNDO_CHAR = "\u21B6";
    String RESET_CHAR = "\u2604";
    String VALID = "\u2714";
    String INVALID = "\u2715";
    int[] TEXT_COLOR_CODES = new int[] { 0, 170, 43520, 43690, 11141120, 11141290, 16755200, 11184810, 5592405, 5592575, 5635925, 5636095, 16733525, 16733695, 16777045, 16777215,
            0, 42, 10752, 10794, 2752512, 2752554, 2763264, 2763306, 1381653, 1381695, 1392405, 1392447, 4134165, 4134207, 4144917, 4144959 };

    default int getColorFromFormattingCharacter(char c, boolean isLighter) {
        return TEXT_COLOR_CODES[isLighter ? "0123456789abcdef".indexOf(c) : "0123456789abcdef".indexOf(c) + 16];
    }

    /**
     * Draws a left-aligned string, with a scrolling effect if the string is too long.
     */
    default void drawScrollingString(Font font, Component text, int minX, int maxX, int y, int color) {
        int maxWidth = maxX - minX;
        int textWidth = font.width(text.getVisualOrderText());
        if (textWidth <= maxWidth) {
            self().drawString(font, text, minX, y, color);
        } else {
            AbstractWidget.renderScrollingString(self(), font, text, minX, y - 1, maxX, y + font.lineHeight, color);
        }
    }

    default void blitInscribed(ResourceLocation texture, int x, int y, int boundsWidth, int boundsHeight, int rectWidth, int rectHeight) {
        this.blitInscribed(texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight, true, true);
    }

    default void blitInscribed(ResourceLocation texture, int x, int y, int boundsWidth, int boundsHeight, int rectWidth, int rectHeight, boolean centerX, boolean centerY) {
        if (rectWidth * boundsHeight > rectHeight * boundsWidth) {
            int h = boundsHeight;
            boundsHeight = (int) (boundsWidth * ((double) rectHeight / rectWidth));
            if (centerY) y += (h - boundsHeight) / 2;
        } else {
            int w = boundsWidth;
            boundsWidth = (int) (boundsHeight * ((double) rectWidth / rectHeight));
            if (centerX) x += (w - boundsWidth) / 2;
        }

        self().blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, boundsWidth, boundsHeight, rectWidth, rectHeight, rectWidth, rectHeight);
    }
}
