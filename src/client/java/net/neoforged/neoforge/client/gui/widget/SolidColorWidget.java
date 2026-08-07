/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.widget;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

/// Renders a solid color in the space of the widget, with drop shadow. On creation, the color and shadow color are transparent; use [#setColor(int)] and [#setShadowColor(int)] to change the colors.
///
/// The drop shadow is rendered outside the widget bounds, alike to the drop shadow of text.
public class SolidColorWidget extends AbstractWidget {
    private int color;
    private int shadowColor;

    /// Creates a new instance of this widget.
    ///
    /// @param x      the X position
    /// @param y      the Y position
    /// @param width  the width of the widget
    /// @param height the height of the widget
    public SolidColorWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    /// Creates a new instance of this widget.
    ///
    /// @param width  the width of the widget
    /// @param height the height of the widget
    public SolidColorWidget(int width, int height) {
        this(0, 0, width, height);
    }

    /// Sets the main color.
    ///
    /// @param color the new main color
    /// @return this instance, for chaining
    @CanIgnoreReturnValue
    public SolidColorWidget setColor(int color) {
        this.color = color;
        return this;
    }

    /// {@return the main color}
    public int getColor() {
        return color;
    }

    /// Sets the shadow color.
    ///
    /// @param shadowColor the new shadow color
    /// @return this instance, for chaining
    @CanIgnoreReturnValue
    public SolidColorWidget setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
        return this;
    }

    /// {@return the shadow color}
    public int getShadowColor() {
        return shadowColor;
    }

    /// Calculate the shadow color based off the main color.
    ///
    /// The calculation is the same used for the drop shadow of text: 25% of the original RGB color (alpha preserved).
    ///
    /// @return this instance, for chaining
    @CanIgnoreReturnValue
    public SolidColorWidget calculateShadow() {
        return setShadowColor(ARGB.scaleRGB(this.getColor(), 0.25F));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.fill(RenderPipelines.GUI, getX() + 1, getY() + 1, getX() + 1 + getWidth(), getY() + 1 + getHeight(), shadowColor);
        graphics.fill(RenderPipelines.GUI, getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false; // Never active
    }

    @Override
    protected void handleCursor(GuiGraphicsExtractor graphics) {
        // Do nothing
    }
}
