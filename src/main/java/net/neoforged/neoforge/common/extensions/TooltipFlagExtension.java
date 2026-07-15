/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

/**
 * Extension methods for {@link net.minecraft.world.item.TooltipFlag}
 */
public interface TooltipFlagExtension {
    /**
     * {@return the state of the Control key (as reported by Screen) on the client, or {@code false} on the server}
     */
    default boolean hasControlDown() {
        return false;
    }

    /**
     * {@return the state of the Shift key (as reported by Screen) on the client, or {@code false} on the server}
     */
    default boolean hasShiftDown() {
        return false;
    }

    /**
     * {@return the state of the Alt key (as reported by Screen) on the client, or {@code false} on the server}
     */
    default boolean hasAltDown() {
        return false;
    }

    /**
     * {@return if the tooltip should provide all information that it may show under varying circumstances} For example, some mods hide extra information by requiring a
     * player to hold a key down (like SHIFT). These mods can choose to provide this extra information to recipe viewers unconditionally, so that the tooltip can be fully
     * indexed and searched.
     */
    default boolean shouldDisplayAllInformation() {
        return false;
    }
}
