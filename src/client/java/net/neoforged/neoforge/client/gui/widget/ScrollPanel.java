/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.widget;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Abstract scroll panel class.
 */
public abstract class ScrollPanel extends AbstractWidget implements ContainerEventHandler {
    private final Minecraft client;
    private boolean scrolling;
    protected float scrollDistance;
    protected boolean captureMouse = true;
    protected final int border;

    private final int barWidth;
    private final int barLeft;
    private final int barBgColor;
    private final int barColor;
    private final int barBorderColor;

    /**
     * @param client the minecraft instance this ScrollPanel should use
     * @param width  the width
     * @param height the height
     * @param y      the offset from the top (y coord)
     * @param x   the offset from the left (x coord)
     * @param component      The component used by the narrator to describe this widget
     */
    public ScrollPanel(Minecraft client, int width, int height, int y, int x, Component component) {
        this(client, width, height, y, x, 4, component);
    }

    /**
     * @param client the minecraft instance this ScrollPanel should use
     * @param width  the width
     * @param height the height
     * @param y      the offset from the top (y coord)
     * @param x      the offset from the left (x coord)
     * @param border the size of the border
     * @param component      The component used by the narrator to describe this widget
     */
    public ScrollPanel(Minecraft client, int width, int height, int y, int x, int border, Component component) {
        this(client, width, height, y, x, border, 6, component);
    }

    /**
     * @param client   the minecraft instance this ScrollPanel should use
     * @param width    the width
     * @param height   the height
     * @param y        the offset from the top (y coord)
     * @param x        the offset from the left (x coord)
     * @param border   the size of the border
     * @param barWidth the width of the scroll bar
     * @param component      The component used by the narrator to describe this widget
     */
    public ScrollPanel(Minecraft client, int width, int height, int y, int x, int border, int barWidth, Component component) {
        this(client, width, height, y, x, border, barWidth, 0xFF000000, 0xFF808080, 0xFFC0C0C0, component);
    }

    /**
     * Base constructor
     *
     * @param client         the minecraft instance this ScrollPanel should use
     * @param width          the width
     * @param height         the height
     * @param y              the offset from the y (y coord)
     * @param x              the offset from the left (x coord)
     * @param border         the size of the border
     * @param barWidth       the width of the scroll bar
     * @param barBgColor     the color for the scroll bar background
     * @param barColor       the color for the scroll bar handle
     * @param barBorderColor the border color for the scroll bar handle
     * @param component      The component used by the narrator to describe this widget
     */
    public ScrollPanel(Minecraft client, int width, int height, int y, int x, int border, int barWidth, int barBgColor, int barColor, int barBorderColor, Component component) {
        super(x, y, width, height, component);
        this.client = client;
        this.width = width;
        this.height = height;
        this.barLeft = this.getX() + this.width - barWidth;
        this.border = border;
        this.barWidth = barWidth;
        this.barBgColor = barBgColor;
        this.barColor = barColor;
        this.barBorderColor = barBorderColor;
    }

    protected abstract int getContentHeight();

    /**
     * Draws the background of the scroll panel. This runs AFTER Scissors are enabled.
     */
    protected void drawBackground(GuiGraphics guiGraphics, float partialTick) {
        Screen.renderMenuBackgroundTexture(guiGraphics, Screen.MENU_BACKGROUND, this.getX(), this.getY(), 0f, 0f, this.width, this.height);
    }

    /**
     * Draw anything special on the screen. Scissor (RenderSystem.enableScissor) is enabled
     * for anything that is rendered outside the view box. Do not mess with Scissor unless you support this.
     */
    protected abstract void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, int mouseX, int mouseY);

    protected boolean clickPanel(double mouseX, double mouseY, MouseButtonEvent event) {
        return false;
    }

    private int getMaxScroll() {
        return this.getContentHeight() - (this.height - this.border);
    }

    private void applyScrollLimits() {
        int max = getMaxScroll();

        if (max < 0) {
            max /= 2;
        }

        if (this.scrollDistance < 0.0F) {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > max) {
            this.scrollDistance = max;
        }
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_, double p_294830_) {
        if (p_294830_ != 0) {
            this.scrollDistance += (float) (-p_294830_ * getScrollAmount());
            applyScrollLimits();
            return true;
        }
        return false;
    }

    protected int getScrollAmount() {
        return 20;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.getX() && mouseX < this.getRight() &&
                mouseY >= this.getY() && mouseY < this.getBottom();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return super.getRectangle();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick))
            return true;

        this.scrolling = event.button() == 0 && event.x() >= barLeft && event.x() < getRight() && event.y() >= this.getY() && event.y() < getBottom();
        if (this.scrolling) {
            return true;
        }
        int mouseListY = ((int) event.y()) - this.getY() - this.getContentHeight() + (int) this.scrollDistance - border;
        if (event.x() >= this.getX() && event.x() < getRight() && mouseListY < 0) {
            return this.clickPanel(event.x() - this.getX(), event.y() - this.getY() + (int) this.scrollDistance - border, event);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (super.mouseReleased(event))
            return true;
        boolean ret = this.scrolling;
        this.scrolling = false;
        return ret;
    }

    private int getBarHeight() {
        int barHeight = (height * height) / this.getContentHeight();

        if (barHeight < 32) barHeight = 32;

        if (barHeight > height - border * 2)
            barHeight = height - border * 2;

        return barHeight;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.scrolling) {
            int maxScroll = height - getBarHeight();
            double moved = deltaY / maxScroll;
            this.scrollDistance += getMaxScroll() * moved;
            applyScrollLimits();
            return true;
        }
        return false;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());

        this.drawBackground(guiGraphics, partialTick);

        int baseY = this.getY() + border - (int) this.scrollDistance;
        this.drawPanel(guiGraphics, getRight(), baseY, mouseX, mouseY);

        int extraHeight = (this.getContentHeight() + border) - height;
        if (extraHeight > 0) {
            int barHeight = getBarHeight();

            int barTop = (int) this.scrollDistance * (height - barHeight) / extraHeight + this.getY();
            if (barTop < this.getY()) {
                barTop = this.getY();
            }

            guiGraphics.fill(this.barLeft, this.getY(), this.barLeft + this.barWidth, this.getBottom(), this.barBgColor);

            guiGraphics.fill(this.barLeft, barTop, this.barLeft + this.barWidth, barTop + barHeight, this.barColor);

            guiGraphics.fill(this.barLeft, barTop, this.barLeft + this.barWidth - 1, barTop + barHeight - 1, this.barBorderColor);
        }

        guiGraphics.disableScissor();
    }

    protected void drawGradientRect(GuiGraphics guiGraphics, int left, int top, int right, int bottom, int color1, int color2) {
        guiGraphics.fillGradient(left, top, right, bottom, color1, color2);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    public int getBottom() {
        return this.getY() + this.height;
    }

    public int getRight() {
        return this.getX() + this.width;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> widgetConsumer) {}

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    private GuiEventListener focused;
    private boolean isDragging;

    @Override
    public final boolean isDragging() {
        return this.isDragging;
    }

    @Override
    public final void setDragging(boolean isDragging) {
        this.isDragging = isDragging;
    }

    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(GuiEventListener listener) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (listener != null) {
            listener.setFocused(true);
        }

        this.focused = listener;
    }
}
