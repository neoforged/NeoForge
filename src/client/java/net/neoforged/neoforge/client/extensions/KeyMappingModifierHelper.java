/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public final class KeyMappingModifierHelper {
    private KeyMappingModifierHelper() {}

    /// Returns whether an incoming key press activates a key mapping.
    ///
    /// The key press must match the mapping's bound key, the conflict context must be
    /// active, and the mapping's [KeyModifier] must be active.
    ///
    /// [NONE][KeyModifier#NONE] is context-sensitive. When
    /// [requiresExactKeyModifierNone()][IKeyConflictContext#requiresExactKeyModifierNone()]
    /// is true, a mapping with [NONE][KeyModifier#NONE] only matches when no modifier keys are held.
    /// When it is false, [NONE][KeyModifier#NONE] matches even when irrelevant modifiers are held.
    ///
    /// Examples:
    ///
    /// - In [GUI][KeyConflictContext#GUI], a mapping bound to `A` with [NONE][KeyModifier#NONE]
    ///   matches `A` only when the user is not holding [Shift][KeyModifier#SHIFT],
    ///   [Control][KeyModifier#CONTROL], [Command][KeyModifier#CONTROL_OR_COMMAND], or
    ///   [Alt][KeyModifier#ALT], because GUI contexts require exact [NONE][KeyModifier#NONE]
    ///   matching ([requiresExactKeyModifierNone()][IKeyConflictContext#requiresExactKeyModifierNone()]
    ///   is true).
    /// - In [IN_GAME][KeyConflictContext#IN_GAME], the same `A` mapping can still match while
    ///   [Shift][KeyModifier#SHIFT], [Control][KeyModifier#CONTROL],
    ///   [Command][KeyModifier#CONTROL_OR_COMMAND], or [Alt][KeyModifier#ALT] is held, because
    ///   in-game contexts treat [NONE][KeyModifier#NONE] as no additional modifier requirement
    ///   ([requiresExactKeyModifierNone()][IKeyConflictContext#requiresExactKeyModifierNone()]
    ///   is false).
    /// - A mapping bound to `A` with [Shift][KeyModifier#SHIFT] only matches `A` while
    ///   [Shift][KeyModifier#SHIFT] is active. Additional held modifiers, such as
    ///   [Control][KeyModifier#CONTROL], do not prevent it from matching.
    /// - A mapping bound directly to the left [Shift][KeyModifier#SHIFT] key with
    ///   [NONE][KeyModifier#NONE] matches the left [Shift][KeyModifier#SHIFT] key, even in
    ///   [GUI][KeyConflictContext#GUI]. Here, [NONE][KeyModifier#NONE] means the mapping has no
    ///   additional modifier requirement; the bound key itself may still be [Shift][KeyModifier#SHIFT],
    ///   [Control][KeyModifier#CONTROL], [Command][KeyModifier#CONTROL_OR_COMMAND], or
    ///   [Alt][KeyModifier#ALT].
    ///
    /// @param keyCode         the incoming key press to test
    /// @param boundKey        the mapping's configured key
    /// @param conflictContext the mapping's conflict context
    /// @param keyModifier     the mapping's modifier requirement
    /// @return `true` if the key press matches the bound key, the conflict context is active, and the
    ///         modifier requirement is active
    /// @see IKeyConflictContext#requiresExactKeyModifierNone()
    /// @see KeyModifier#isActive(IKeyConflictContext)
    /// @see #isActiveAndMatches(InputConstants.Key, InputConstants.Key, IKeyConflictContext, boolean)
    public static boolean isActiveAndMatches(InputConstants.Key keyCode, InputConstants.Key boundKey, IKeyConflictContext conflictContext, KeyModifier keyModifier) {
        return isActiveAndMatches(keyCode, boundKey, conflictContext, isModifierActive(conflictContext, keyModifier, boundKey));
    }

    /// Variant of [#isActiveAndMatches(InputConstants.Key, InputConstants.Key, IKeyConflictContext, KeyModifier)]
    /// for callers that have already checked whether the mapping's modifier
    /// requirement is active.
    ///
    /// @param keyCode         the incoming key press to test
    /// @param boundKey        the mapping's configured key
    /// @param conflictContext the mapping's conflict context
    /// @param modifierActive  whether the mapping's modifier requirement is active
    /// @return `true` if the key press matches the bound key, the conflict context is active, and
    ///         `modifierActive` is true
    /// @see #isActiveAndMatches(InputConstants.Key, InputConstants.Key, IKeyConflictContext, KeyModifier)
    public static boolean isActiveAndMatches(InputConstants.Key keyCode, InputConstants.Key boundKey, IKeyConflictContext conflictContext, boolean modifierActive) {
        return keyCode != InputConstants.UNKNOWN && keyCode.equals(boundKey) && conflictContext.isActive() && modifierActive;
    }

    /// Returns whether two key mappings conflict in the controls screen.
    ///
    /// Two mappings conflict when their contexts conflict and either:
    ///
    /// - one mapping's bound key is the other mapping's modifier, such as
    ///   the left [Shift][KeyModifier#SHIFT] key conflicting with an `A` mapping that uses
    ///   [Shift][KeyModifier#SHIFT], or
    /// - both mappings use the same bound key and their modifiers conflict, such as
    ///   two `A` mappings that both use [Shift][KeyModifier#SHIFT].
    ///
    /// A same-key bare mapping only conflicts with a modified mapping when the bare
    /// mapping's context does not require exact [NONE][KeyModifier#NONE] matching.
    /// For example, `A` conflicts with an `A` mapping that uses [Shift][KeyModifier#SHIFT] when
    /// [requiresExactKeyModifierNone()][IKeyConflictContext#requiresExactKeyModifierNone()]
    /// returns false. When it returns true, the same two mappings can coexist because bare `A`
    /// does not activate while [Shift][KeyModifier#SHIFT] is held.
    ///
    /// @param first  the first key mapping to compare
    /// @param second the second key mapping to compare
    /// @return `true` if the two key mappings conflict
    /// @see KeyMapping#same(KeyMapping)
    /// @see #hasKeyModifierConflict(KeyMapping, KeyMapping)
    public static boolean hasKeyMappingConflict(KeyMapping first, KeyMapping second) {
        return hasKeyMappingConflict(
                first.getKey(), first.getKeyConflictContext(), first.getKeyModifier(),
                second.getKey(), second.getKeyConflictContext(), second.getKeyModifier());
    }

    /// Returns whether either mapping uses the other mapping's bound key as its
    /// modifier key.
    ///
    /// For example, a bare mapping bound directly to the left [Shift][KeyModifier#SHIFT] key conflicts
    /// with an `A` mapping that uses [Shift][KeyModifier#SHIFT] when their contexts conflict.
    /// This method does not report same-key conflicts like two `A` mappings; use
    /// [#hasKeyMappingConflict(KeyMapping, KeyMapping)] for the full conflict
    /// check used by [KeyMapping#same(KeyMapping)].
    ///
    /// @param first  the first key mapping to compare
    /// @param second the second key mapping to compare
    /// @return `true` if either mapping uses the other mapping's bound key as its modifier
    /// @see #hasKeyMappingConflict(KeyMapping, KeyMapping)
    public static boolean hasKeyModifierConflict(KeyMapping first, KeyMapping second) {
        return contextsConflict(first.getKeyConflictContext(), second.getKeyConflictContext()) &&
                keyModifierMatchesOtherKey(first.getKey(), first.getKeyModifier(), second.getKey(), second.getKeyModifier());
    }

    private static boolean hasKeyMappingConflict(InputConstants.Key firstKey, IKeyConflictContext firstContext, KeyModifier firstModifier, InputConstants.Key secondKey, IKeyConflictContext secondContext, KeyModifier secondModifier) {
        if (!contextsConflict(firstContext, secondContext)) {
            return false;
        }

        return keyModifierMatchesOtherKey(firstKey, firstModifier, secondKey, secondModifier) ||
                (firstKey.equals(secondKey) && keyModifiersConflict(firstContext, firstModifier, secondContext, secondModifier));
    }

    private static boolean keyModifierMatchesOtherKey(InputConstants.Key firstKey, KeyModifier firstModifier, InputConstants.Key secondKey, KeyModifier secondModifier) {
        return firstModifier.matches(secondKey) || secondModifier.matches(firstKey);
    }

    private static boolean isBareModifierKey(KeyModifier keyModifier, InputConstants.Key boundKey) {
        return keyModifier == KeyModifier.NONE && KeyModifier.isKeyCodeModifier(boundKey);
    }

    static boolean isModifierActive(IKeyConflictContext conflictContext, KeyModifier keyModifier, InputConstants.Key boundKey) {
        return isBareModifierKey(keyModifier, boundKey) || keyModifier.isActive(conflictContext);
    }

    private static boolean keyModifiersConflict(IKeyConflictContext firstContext, KeyModifier firstModifier, IKeyConflictContext secondContext, KeyModifier secondModifier) {
        if (firstModifier == secondModifier) {
            return true;
        }

        if (firstModifier != KeyModifier.NONE && secondModifier != KeyModifier.NONE) {
            return false;
        }

        IKeyConflictContext noModifierContext = firstModifier == KeyModifier.NONE ? firstContext : secondContext;
        return !noModifierContext.requiresExactKeyModifierNone();
    }

    private static boolean contextsConflict(IKeyConflictContext firstContext, IKeyConflictContext secondContext) {
        return firstContext.conflicts(secondContext) || secondContext.conflicts(firstContext);
    }
}
