/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.platform.Window;
import net.neoforged.fml.loading.EarlyLoadingScreenController;
import org.lwjgl.sdl.SDLVideo;

public final class EarlyWindowHandoff {
    /// Completes the renderer handoff and restores the early display's native window state.
    public static void completeWindowHandoff(Window window) {
        EarlyLoadingScreenController earlyLoadingScreen = EarlyLoadingScreenController.current();
        if (earlyLoadingScreen == null) {
            SDLVideo.SDL_ShowWindow(window.handle());
            return;
        }

        EarlyLoadingScreenController.WindowState state = earlyLoadingScreen.handOverToMinecraft(() -> new Blaze3DRenderBackend(window));
        long windowHandle = window.handle();

        int x;
        int y;
        int width;
        int height;
        if (state.posValid() && !state.minimized()) {
            x = state.x();
            y = state.y();
        } else {
            x = window.getX();
            y = window.getY();
        }
        if (state.maximized()) {
            // A maximized window reports its maximized size, not its restore size.
            SDLVideo.SDL_MaximizeWindow(windowHandle);
            width = window.getWidth();
            height = window.getHeight();
            window.setWindowed(state.width(), state.height());
        } else {
            width = state.width();
            height = state.height();
        }
        window.setWindowSizeAndPosition(x, y, width, height);

        if (state.minimized() && !window.isWindowFullscreen()) {
            SDLVideo.SDL_MinimizeWindow(windowHandle);
        }
        window.syncWindow();
        SDLVideo.SDL_ShowWindow(windowHandle);
    }

    private EarlyWindowHandoff() {}
}
