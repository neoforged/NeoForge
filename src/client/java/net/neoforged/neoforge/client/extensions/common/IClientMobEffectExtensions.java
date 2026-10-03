/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.fml.LogicalSide;

/// [Client-only][LogicalSide#CLIENT] extensions to [MobEffect]
///
/// @see RegisterClientExtensionsEvent
public interface IClientMobEffectExtensions {
    IClientMobEffectExtensions DEFAULT = new IClientMobEffectExtensions() {};

    static IClientMobEffectExtensions of(MobEffectInstance instance) {
        return of(instance.getEffect().value());
    }

    static IClientMobEffectExtensions of(MobEffect effect) {
        return ClientExtensionsManager.MOB_EFFECT_EXTENSIONS.getOrDefault(effect, DEFAULT);
    }

    /// @return whether the given effect should be shown in the player's inventory.
    /// By default, this returns `true`.
    default boolean isVisibleInInventory(MobEffectInstance instance) {
        return true;
    }

    /// @return whether the given effect should be shown in the HUD.
    /// By default, this returns `true`.
    default boolean isVisibleInGui(MobEffectInstance instance) {
        return true;
    }

    /// Renders the icon of the specified effect in the player's inventory.
    ///
    /// @param instance The effect instance
    /// @param screen   The effect-rendering screen
    /// @param graphics The gui graphics
    /// @param x        The x coordinate to render at
    /// @param y        The y coordinate to render at
    /// @param width    Available width of canvas to render in
    /// @param height   Available height of canvas to render in
    /// @param color    Color multiplicator of the icon
    /// @return `true` to prevent default rendering, `false` otherwise
    default boolean extractInventoryIcon(MobEffectInstance instance, AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        return false;
    }

    /// Renders the text of the specified effect in the player's inventory.
    ///
    /// @param instance    The effect instance
    /// @param screen      The effect-rendering screen
    /// @param graphics    The gui graphics
    /// @param x           The x coordinate
    /// @param y           The y coordinate
    /// @param canvasWidth Available pixels, where rendering anything wider than this amount of pixels may overlap with other GUI elements or go out of bounds
    /// @param color       Desired color of text, serving as a hint of best color to use for it to be distinct from background
    /// @return `true` to prevent default rendering, `false` otherwise
    default boolean extractInventoryText(MobEffectInstance instance, AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics, int x, int y, int canvasWidth, int color) {
        return false;
    }

    /// Renders the icon of the specified effect on the player's HUD.
    /// This can be used to render icons from your own texture sheet.
    ///
    /// @param instance The effect instance
    /// @param hud      The HUD
    /// @param graphics The gui graphics
    /// @param x        The x coordinate
    /// @param y        The y coordinate
    /// @param width    Available width of canvas to render in
    /// @param height   Available height of canvas to render in
    /// @param color    Color multiplicator of the icon
    /// @return `true` to prevent default rendering, `false` otherwise
    default boolean extractHudIcon(MobEffectInstance instance, Hud hud, GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        return false;
    }
}
