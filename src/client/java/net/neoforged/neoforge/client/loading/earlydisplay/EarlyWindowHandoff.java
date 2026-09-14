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

        if (!window.isWindowFullscreen()) {
            if (state.posValid() && !state.minimized()) {
                SDLVideo.SDL_SetWindowPosition(windowHandle, state.x(), state.y());
                window.onMove(state.x(), state.y());
            }

            if (state.maximized()) {
                // A maximized window reports its maximized size, not its restore size.
                SDLVideo.SDL_MaximizeWindow(windowHandle);
            } else {
                SDLVideo.SDL_SetWindowSize(windowHandle, state.width(), state.height());
            }
            window.onResize(state.width(), state.height());
            window.onFramebufferResize(state.width(), state.height());

            if (state.minimized()) {
                SDLVideo.SDL_MinimizeWindow(windowHandle);
                window.onIconified(true);
            }
        }

        window.syncWindow();
        SDLVideo.SDL_ShowWindow(windowHandle);
        earlyLoadingScreen.periodicTick();
    }

    private EarlyWindowHandoff() {}
}
