/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

/**
 * Register an instance to {@link ModContainer#registerExtensionPoint(Class, Supplier)}
 * to supply a config screen for your mod.
 *
 * <p>The config screen will be accessible from the mod list menu.
 */
public interface IConfigScreenFactory extends IExtensionPoint {
    /**
     * Creates a new config screen. The {@code modListScreen} parameter can be used for a "back" button.
     */
    Screen createScreen(Minecraft minecraft, Screen modListScreen);

    static Optional<IConfigScreenFactory> getForMod(IModInfo selectedMod) {
        return ModList.get().getModContainerById(selectedMod.getModId()).flatMap(m -> m.getCustomExtension(IConfigScreenFactory.class));
    }
}
