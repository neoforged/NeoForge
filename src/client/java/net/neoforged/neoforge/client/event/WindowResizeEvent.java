/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.platform.Window;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/// Fired when the Minecraft window is resizing, including GUI scale changes, game switches fullscreen mode, and the unicode font option is changed.
///
/// @see net.minecraft.client.Minecraft#resizeGui()
public class WindowResizeEvent extends Event {
    private final Window window;

    @ApiStatus.Internal
    public WindowResizeEvent(Window window) {
        this.window = window;
    }

    /// {@return the window}
    public Window getWindow() {
        return window;
    }
}
