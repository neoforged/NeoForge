/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import net.minecraft.client.Minecraft;

public class DebuggingHelper {

    private DebuggingHelper() {
        throw new IllegalStateException("Tried to create utility class!");
    }

    public static boolean releaseMouse() {
        Minecraft.getInstance().mouseHandler.releaseMouse();
        return true;
    }
}
