/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.util;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

/**
 * Debugging helper class.
 * @implNote This class is not intended for use in production builds, however although it is marked as internal, modders should feel free to use it during their debugging sessions.
 */
@ApiStatus.Internal
public final class DebuggingHelper {

    private DebuggingHelper() {
        throw new IllegalStateException("Tried to create utility class!");
    }

    /**
     * Utility method to release the mouse.
     * <p>
     *     Useful for debugging, as it allows you to move the mouse outside of the game window, if your window manager does not release it automatically, using a conditional breakpoint.
     * </p>
     * @return {@code true}, always.
     */
    public static boolean releaseMouse() {
        Minecraft.getInstance().mouseHandler.releaseMouse();
        return true;
    }
}
