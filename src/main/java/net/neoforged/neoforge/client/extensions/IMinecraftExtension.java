/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.extensions.ILevelReaderExtension;
import net.neoforged.neoforge.flag.FlagManager;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Extension interface for {@link Minecraft}.
 */
public interface IMinecraftExtension {
    private Minecraft self() {
        return (Minecraft) this;
    }

    /**
     * Pushes a screen as a new GUI layer.
     *
     * @param screen the new GUI layer
     */
    default void pushGuiLayer(Screen screen) {
        ClientHooks.pushGuiLayer(self(), screen);
    }

    /**
     * Pops a GUI layer from the screen.
     */
    default void popGuiLayer() {
        ClientHooks.popGuiLayer(self());
    }

    /**
     * Retrieves the {@link Locale} set by the player.
     * Useful for creating string and number formatters.
     */
    default Locale getLocale() {
        return self().getLanguageManager().getJavaLocale();
    }

    /**
     * Returns the clients {@link FlagManager}.
     * <p>
     * This instance should receive updates from the server as needed.
     * Prefer using {@link ILevelReaderExtension#getModdedFlagManager()} where possible.
     *
     * @return client sided {@link FlagManager}.
     */
    default FlagManager getModdedFlagManager() {
        throw new NotImplementedException("IMinecraftExtension#getModdedFlagManager must be implemented to lookup the clients FlagManager");
    }
}
