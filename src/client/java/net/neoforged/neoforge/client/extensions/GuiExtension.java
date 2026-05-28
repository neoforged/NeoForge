/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.ClientHooks;

public interface GuiExtension {
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

    private Gui self() {
        return (Gui) this;
    }
}
