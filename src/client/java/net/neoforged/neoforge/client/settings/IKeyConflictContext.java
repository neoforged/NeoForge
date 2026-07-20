/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.settings;

import net.minecraft.client.KeyMapping;

/**
 * Defines the context that a {@link KeyMapping} is used.
 * Key conflicts occur when a {@link KeyMapping} has the same {@link IKeyConflictContext} and has conflicting modifiers and keyCodes.
 */
public interface IKeyConflictContext {
    /**
     * @return true if conditions are met to activate {@link KeyMapping}s with this context
     */
    boolean isActive();

    /**
     * @return true if the other context can have {@link KeyMapping} conflicts with this one.
     *         This will be called on both contexts to check for conflicts.
     */
    boolean conflicts(IKeyConflictContext other);

    /**
     * {@return true if mappings using {@link KeyModifier#NONE} should only match
     * when no modifier keys are held in this context}
     * <p>
     * When this is true, a bare key mapping such as {@code A} does not match while
     * Shift, Control, Command, or Alt is held. This allows {@code A} and
     * {@code Shift+A} to coexist without conflicts.
     * <p>
     * When this is false, {@link KeyModifier#NONE} means the mapping has no separate
     * modifier requirement. A bare key mapping such as {@code A} can still match while
     * Shift is held, so {@code A} conflicts with {@code Shift+A}.
     */
    default boolean requiresExactKeyModifierNone() {
        return true;
    }
}
