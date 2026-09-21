/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.Collection;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/// Declares the screen areas occupied by a UI, so that other UIs can query and avoid them.
///
/// Providers are registered via [RegisterScreenAreaProviderEvent][net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent]
/// and are queried on demand, so the declared areas may change between calls.
@FunctionalInterface
public interface ScreenAreaProvider {
    /// Returns the areas currently occupied by this UI, in GUI-scaled absolute screen
    /// coordinates (origin at the top-left, y pointing down).
    ///
    /// An empty collection may be returned to declare that nothing is currently occupied
    /// (for example because the UI is hidden). Degenerate areas (width or height `<= 0`)
    /// are ignored.
    ///
    /// @param context the context the areas are queried in
    /// @return the occupied areas
    Collection<ScreenRectangle> getAreas(ScreenAreaContext context);
}
