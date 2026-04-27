/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import java.util.function.UnaryOperator;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@ApiStatus.Internal
public interface ConfigurationScreenFactory {
    // unary operator takes in previous screen (to return to later)
    @Nullable
    UnaryOperator<Screen> create(ModDisplayInfo displayInfo);
}
