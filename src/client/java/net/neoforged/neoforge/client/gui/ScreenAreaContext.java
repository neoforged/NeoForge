/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

/// The context in which [screen area providers][ScreenAreaProvider] are queried.
///
/// @param screen    the currently open screen, or `null` if no screen is open (the HUD is visible)
/// @param guiWidth  the current GUI-scaled screen width
/// @param guiHeight the current GUI-scaled screen height
public record ScreenAreaContext(@Nullable Screen screen, int guiWidth, int guiHeight) {
    /// {@return true if no screen is currently open and only the HUD is visible}
    public boolean isHudContext() {
        return this.screen == null;
    }
}
