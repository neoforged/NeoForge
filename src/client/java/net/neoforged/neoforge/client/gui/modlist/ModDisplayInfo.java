/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import java.net.URI;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.IExtensionPoint;
import org.jspecify.annotations.Nullable;

// TODO: reconsider if extension point is the best way to do this

/// An extension point for information displayed on the [mod list screen][ModListScreen].
public interface ModDisplayInfo extends IExtensionPoint {
    String id();

    Component displayName();

    String version();

    Component authors();

    Component credits();

    Component description();

    Component license();

    @Nullable
    ImageResource logo(); // rendered as rectangle

    @Nullable
    ImageResource icon(); // rendered as a square

    @Nullable
    URI displayUrl();

    @Nullable
    URI issuesUrl();
}
