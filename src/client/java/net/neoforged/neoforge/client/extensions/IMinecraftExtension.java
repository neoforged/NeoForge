/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import java.util.Locale;
import net.minecraft.client.Minecraft;

/**
 * Extension interface for {@link Minecraft}.
 */
public interface IMinecraftExtension {
    private Minecraft self() {
        return (Minecraft) this;
    }

    /**
     * Retrieves the {@link Locale} set by the player.
     * Useful for creating string and number formatters.
     */
    default Locale getLocale() {
        return self().getLanguageManager().getJavaLocale();
    }
}
