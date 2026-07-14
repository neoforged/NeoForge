/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import java.net.URI;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jspecify.annotations.Nullable;

/// An extension point for information displayed on the [mod list screen][ModListScreen].
///
/// Unless otherwise stated in the documentation, provided components may contain [click actions][net.minecraft.network.chat.ClickEvent]
/// and [hover actions][net.minecraft.network.chat.HoverEvent].
///
/// @see IConfigScreenFactory Extension point for custom configuration screens
public interface ModDisplayInfo extends IExtensionPoint {
    /// {@return the mod ID} This is used to retrieve the mod container, [configuration screen extension point][IConfigScreenFactory],
    /// and other relevant information.
    String id();

    /// {@return the display name} This is always displayed, even if [empty][Component#empty()].
    /// Click actions do not work on this component.
    Component displayName();

    /// {@return the mod version} This is always displayed, even if [empty][Component#empty()].
    String version();

    /// {@return the mod authors} This is displayed if it is not [an empty component][Component#empty()].
    Component authors();

    /// {@return the credits} This is displayed if it is not [an empty component][Component#empty()].
    Component credits();

    /// {@return the mod authors} This is displayed if it is not [an empty component][Component#empty()].
    Component description();

    /// {@return the mod license} This is always displayed, even if [empty][Component#empty()].
    Component license();

    /// {@return the banner displayed in the info panel, or `null`}
    ///
    /// The banner is rendered with its original aspect ratio, bounded by the width of the info panel (ordinarily
    /// {@value ModListScreen#INFO_PANEL_WIDTH} pixels) and a maximum height of {@value ModListScreen#BANNER_HEIGHT} pixels.
    @Nullable
    ImageResource banner(); // rendered as rectangle

    /// {@return the icon displayed in the mod list, or `null`}
    ///
    /// The icon is rendered as a square with sides of {@value ModListScreen#ICON_SIZE} pixels.
    @Nullable
    ImageResource icon(); // rendered as a square

    /// {@return the URL for the mod homepage, or `null`} If `null`, the homepage button is disabled.
    @Nullable
    URI displayUrl();

    /// {@return the URL for the mod issues page, or `null`} If `null`, the issues page button is disabled.
    @Nullable
    URI issuesUrl();
}
