/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.extensions;

import net.minecraft.client.gui.components.AbstractWidget;

/**
 * Extension interface for {@link AbstractWidget}.
 */
public interface IAbstractWidgetExtension
{

    private AbstractWidget self()
    {
        return (AbstractWidget) this;
    }

    /**
     * Handles the logic for when this widget is clicked. Vanilla calls this after {@link AbstractWidget#mouseClicked(double, double, int)} validates that:
     * <ul>
     *     <li>this widget is {@link AbstractWidget#active active} and {@link AbstractWidget#visible visible}</li>
     *     <li>the button {@link AbstractWidget#isValidClickButton(int) can be handled} by this widget</li>
     *     <li>the mouse {@link AbstractWidget#clicked(double, double) is over} this widget</li>
     * </ul>
     *
     * @param mouseX the X position of the mouse
     * @param mouseY the Y position of the mouse
     * @param button the mouse button being clicked
     *
     * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_LEFT
     * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_RIGHT
     * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_MIDDLE
     * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_4
     */
    default void onClick(double mouseX, double mouseY, int button)
    {
        self().onClick(mouseX, mouseY);
    }
}