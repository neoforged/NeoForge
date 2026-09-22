/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.function.Consumer;

/// Declares the screen areas occupied by a UI that is not rendered by a single screen, such as an
/// element of the HUD, so that other UIs can query and avoid them.
///
/// Providers are registered via [RegisterScreenAreaProviderEvent][net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent]
/// or [RegisterGuiLayersEvent][net.neoforged.neoforge.client.event.RegisterGuiLayersEvent]
/// and are queried on demand, so the declared areas may change between queries.
@FunctionalInterface
public interface GlobalScreenAreaProvider {
    /// Collects the areas currently occupied by this UI, in GUI-scaled absolute screen coordinates
    /// (origin at the top-left, y pointing down).
    ///
    /// A provider may declare nothing (for example because the UI is hidden). Degenerate areas
    /// (width or height `<= 0`) are ignored.
    ///
    /// The provider must not query [ScreenAreaManager] while it is being evaluated; the areas of
    /// the providers evaluated before it are available through the given lookup.
    ///
    /// @param earlier the areas declared by the providers evaluated before this one
    /// @param out     accepts the declared areas
    void collectAreas(ScreenAreaLookup earlier, Consumer<ScreenArea> out);
}
