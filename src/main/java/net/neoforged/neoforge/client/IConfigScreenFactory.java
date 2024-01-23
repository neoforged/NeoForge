/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.RegisterConfigScreenEvent;

/**
 * Factory for mod-provided config screens accessed via NeoForge's mod list menu.
 * Register to {@link RegisterConfigScreenEvent}.
 */
public interface IConfigScreenFactory {
    /**
     * Creates a config screen.
     * Setting the screen back to the provided {@code modListScreen} will return the user to the mod list menu.
     */
    Screen createScreen(Minecraft minecraft, Screen modListScreen);
}
