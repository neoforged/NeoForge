/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.fml.LogicalSide;

/**
 * {@linkplain LogicalSide#CLIENT Client-only} extensions to {@link MobEffect}.
 *
 * @see RegisterClientExtensionsEvent
 */
public interface IClientMobEffectExtensions {
    IClientMobEffectExtensions DEFAULT = new IClientMobEffectExtensions() {};

    static IClientMobEffectExtensions of(MobEffectInstance instance) {
        return of(instance.getEffect().value());
    }

    static IClientMobEffectExtensions of(MobEffect effect) {
        return ClientExtensionsManager.MOB_EFFECT_EXTENSIONS.getOrDefault(effect, DEFAULT);
    }

    /**
     * Queries whether the given effect should be shown in the player's inventory.
     * <p>
     * By default, this returns {@code true}.
     */
    default boolean isVisibleInInventory(MobEffectInstance instance) {
        return true;
    }

    /**
     * Queries whether the given effect should be shown in the HUD.
     * <p>
     * By default, this returns {@code true}.
     */
    default boolean isVisibleInGui(MobEffectInstance instance) {
        return true;
    }

    /**
     * Renders the icon of the specified effect in the player's inventory.
     * This can be used to render icons from your own texture sheet.
     *
     * @param instance    The effect instance
     * @param screen      The effect-rendering screen
     * @param guiGraphics The gui graphics
     * @param x           The x coordinate
     * @param y           The y coordinate
     * @param blitOffset  The blit offset
     * @return true to prevent default rendering, false otherwise
     * @deprecated Use {@link IClientMobEffectExtensions#extractInventoryIcon}
     */
    @Deprecated(forRemoval = true, since = "26.1.2")
    default boolean renderInventoryIcon(MobEffectInstance instance, AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int x, int y, int blitOffset) {
        return false;
    }

    /**
     * Renders the icon of the specified effect in the player's inventory.
     *
     * @param instance The effect instance
     * @param screen   The effect-rendering screen
     * @param graphics The gui graphics
     * @param x        The x coordinate to render at
     * @param y        The y coordinate to render at
     * @param width    Available width of canvas to render in
     * @param height   Available height of canvas to render in
     * @param color    Color multiplicator of the icon
     * @return true to prevent default rendering, false otherwise
     */
    default boolean extractInventoryIcon(MobEffectInstance instance, AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        return renderInventoryIcon(instance, screen, graphics, x, y - 7, 0); // -7 for renderInventoryIcon() to receive improperly aligned argument to not break existing mods
    }

    /**
     * Renders the text of the specified effect in the player's inventory.
     *
     * @param instance    The effect instance
     * @param screen      The effect-rendering screen
     * @param guiGraphics The gui graphics
     * @param x           The x coordinate
     * @param y           The y coordinate
     * @param blitOffset  The blit offset
     * @return true to prevent default rendering, false otherwise
     * @deprecated Use {@link IClientMobEffectExtensions#extractInventoryText}
     */
    @Deprecated(forRemoval = true, since = "26.1.2")
    default boolean renderInventoryText(MobEffectInstance instance, AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int x, int y, int blitOffset) {
        return false;
    }

    /**
     * Renders the text of the specified effect in the player's inventory.
     *
     * @param instance    The effect instance
     * @param screen      The effect-rendering screen
     * @param graphics    The gui graphics
     * @param x           The x coordinate
     * @param y           The y coordinate
     * @param canvasWidth Available pixels, rendering anything wider than this amount of pixels may overlap with other GUI elements or go out of bounds
     * @param color       Desired color of text, serving as a hint of best color to use for it to be distinct from background
     * @return true to prevent default rendering, false otherwise
     */
    default boolean extractInventoryText(MobEffectInstance instance, AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics, int x, int y, int canvasWidth, int color) {
        return renderInventoryText(instance, screen, graphics, x - 32, y - 7, 0); // -32 and -7 for renderInventoryText() to receive improperly aligned arguments to not break existing mods
    }

    /**
     * Renders the icon of the specified effect on the player's HUD.
     * This can be used to render icons from your own texture sheet.
     *
     * @param instance    The effect instance
     * @param gui         The gui
     * @param guiGraphics The gui graphics
     * @param x           The x coordinate
     * @param y           The y coordinate
     * @param z           The z depth
     * @param alpha       The alpha value. Blinks when the effect is about to run out
     * @return true to prevent default rendering, false otherwise
     * @deprecated Use {@link IClientMobEffectExtensions#extractGuiIcon}
     */
    @Deprecated(forRemoval = true, since = "26.1.2")
    default boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphicsExtractor guiGraphics, int x, int y, float z, float alpha) {
        return false;
    }

    /**
     * Renders the icon of the specified effect on the player's HUD.
     * This can be used to render icons from your own texture sheet.
     *
     * @param instance The effect instance
     * @param gui      The gui
     * @param graphics The gui graphics
     * @param x        The x coordinate
     * @param y        The y coordinate
     * @param width    Available width of canvas to render in
     * @param height   Available height of canvas to render in
     * @param color    Color multiplicator of the icon
     * @return true to prevent default rendering, false otherwise
     */
    default boolean extractGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        return renderGuiIcon(instance, gui, graphics, x - 3, y - 3, 0f, ARGB.alpha(color) / 255f); // -3 for renderGuiIcon() to receive improperly aligned arguments to not break existing mods
    }
}
