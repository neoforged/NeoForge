/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.settings;

import net.minecraft.client.KeyMapping;

/// Defines the context that a [KeyMapping] is used.
///
/// Key conflicts occur when a [KeyMapping] has the same [IKeyConflictContext]
/// and has conflicting modifiers and key codes.
public interface IKeyConflictContext {
    /// @return true if conditions are met to activate [KeyMapping]s with this context
    boolean isActive();

    /// @return true if the other context can have [KeyMapping] conflicts with this one.
    ///         This will be called on both contexts to check for conflicts.
    boolean conflicts(IKeyConflictContext other);

    /// {@return true if mappings using [NONE][KeyModifier#NONE] should only match
    /// when no modifier keys are held in this context}
    ///
    /// When this is true, a bare key mapping such as `A` does not match while
    /// [Shift][KeyModifier#SHIFT], [Control][KeyModifier#CONTROL],
    /// [Command][KeyModifier#CONTROL_OR_COMMAND], or [Alt][KeyModifier#ALT] is held.
    /// This allows a bare `A` mapping and an `A` mapping with [Shift][KeyModifier#SHIFT]
    /// to coexist without conflicts.
    ///
    /// When this is false, [NONE][KeyModifier#NONE] means the mapping has no separate
    /// modifier requirement. A bare key mapping such as `A` can still match while
    /// [Shift][KeyModifier#SHIFT] is held, so it conflicts with an `A` mapping with
    /// [Shift][KeyModifier#SHIFT].
    default boolean requiresExactKeyModifierNone() {
        return true;
    }
}
