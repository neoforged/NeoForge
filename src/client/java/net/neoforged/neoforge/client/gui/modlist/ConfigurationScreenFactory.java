/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import java.util.function.UnaryOperator;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Configuration screen factory used by the mod list screen. For internal use only.
@FunctionalInterface
@ApiStatus.Internal
interface ConfigurationScreenFactory {
    // unary operator takes in previous screen (to return to later)
    @Nullable
    UnaryOperator<Screen> create(ModDisplayInfo displayInfo);

    ConfigurationScreenFactory DEFAULT = displayInfo -> {
        final ModContainer container = ModList.get().getModContainerById(displayInfo.id()).orElse(null);
        if (container == null) return null;

        final IConfigScreenFactory factory = container.getCustomExtension(IConfigScreenFactory.class).orElse(null);
        if (factory == null) return null;

        return parentScreen -> factory.createScreen(container, parentScreen);
    };
}
