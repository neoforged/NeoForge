/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.function.Consumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jspecify.annotations.Nullable;

/// Declares additional areas occupied by UI attached to a screen.
///
/// Providers can be added through [ScreenEvent.Init#addScreenAreaProvider]. They are queried on
/// demand, so their areas may change between queries. A provider can compare the requester id with
/// its own id to avoid reporting its areas to its owner.
@FunctionalInterface
public interface ScreenAreaProvider {
    /// Collects the currently occupied areas in GUI-scaled absolute screen coordinates.
    ///
    /// @param includePrimary whether the query includes the screen's primary content area
    /// @param requesterId    the id of the UI requesting the areas, or `null` if it has no id
    /// @param out            accepts the occupied areas
    void collectAreas(boolean includePrimary, @Nullable Identifier requesterId, Consumer<ScreenRectangle> out);
}
