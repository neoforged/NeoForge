/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;

/// Declares the screen areas occupied by a screen, so that other UIs can query and avoid them.
///
/// Providers are registered via [RegisterScreenAreaProviderEvent][net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent]
/// and are queried on demand with the live screen instance, so the declared areas may change between queries.
///
/// @param <S> the type of screen the areas are declared for
@FunctionalInterface
public interface ScreenAreaProvider<S extends Screen> {
    /// Collects the areas currently occupied by this UI, in GUI-scaled absolute screen coordinates
    /// (origin at the top-left, y pointing down).
    ///
    /// A provider may declare nothing (for example because the UI is hidden). Degenerate areas
    /// (width or height `<= 0`) are ignored.
    ///
    /// The provider is only queried for screens that are instances of the screen class it was
    /// registered for. It must not query [ScreenAreaManager] while it is being evaluated; the areas
    /// of the providers evaluated before it are available through the given lookup.
    ///
    /// @param screen  the screen the areas are queried for
    /// @param earlier the areas declared by the providers evaluated before this one
    /// @param out     accepts the declared areas
    void collectAreas(S screen, ScreenAreaLookup earlier, Consumer<ScreenArea> out);
}
