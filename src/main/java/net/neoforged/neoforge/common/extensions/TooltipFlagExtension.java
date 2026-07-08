/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

/// Extension methods for [net.minecraft.world.item.TooltipFlag]
public interface TooltipFlagExtension {
    /// {@return the state of the Control key (as reported by Screen) on the client, or `false` on the server}
    default boolean hasControlDown() {
        return false;
    }

    /// {@return the state of the Shift key (as reported by Screen) on the client, or `false` on the server}
    default boolean hasShiftDown() {
        return false;
    }

    /// {@return the state of the Alt key (as reported by Screen) on the client, or `false` on the server}
    default boolean hasAltDown() {
        return false;
    }

    /// {@return if the tooltip should provide recipe viewer specific information} For example if holding certain key combinations changes the resulting tooltips,
    /// this method can be checked to see if all the tooltips should be added instead of just the ones matching the current key combo.
    default boolean isRecipeViewer() {
        return false;
    }
}
