/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.List;
import net.minecraft.resources.Identifier;

/// A read-only view of the areas that have already been declared during the evaluation a
/// [provider][ScreenAreaProvider] is currently being queried in.
///
/// A provider can only see the areas of the providers that are evaluated before it, which is
/// controlled by the `after` ids the providers are registered with, see
/// [RegisterScreenAreaProviderEvent][net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent].
public interface ScreenAreaLookup {
    /// {@return all areas declared by the providers evaluated before the current one}
    List<ScreenArea> all();

    /// {@return the areas declared by the providers evaluated before the current one, except those
    /// declared with the given id}
    ///
    /// @param id the id of the provider whose areas are excluded
    List<ScreenArea> excluding(Identifier id);

    /// {@return the areas declared with the given id by the providers evaluated before the current one}
    ///
    /// @param id the id of the provider whose areas are included
    List<ScreenArea> withId(Identifier id);
}
