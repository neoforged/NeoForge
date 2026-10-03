/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

/// The context in which [screen area providers][ScreenAreaProvider] are queried.
///
/// @param screens   the screens rendered by the GUI, from the bottom-most to the top-most; empty if only the HUD is rendered
/// @param guiWidth  the current GUI-scaled screen width
/// @param guiHeight the current GUI-scaled screen height
public record ScreenAreaContext(List<Screen> screens, int guiWidth, int guiHeight) {
    /// {@return the top-most screen, or `null` if only the HUD is rendered}
    public @Nullable Screen screen() {
        return this.screens.isEmpty() ? null : this.screens.getLast();
    }

    /// {@return true if no screen is currently rendered and only the HUD is visible}
    public boolean isHudContext() {
        return this.screens.isEmpty();
    }
}
