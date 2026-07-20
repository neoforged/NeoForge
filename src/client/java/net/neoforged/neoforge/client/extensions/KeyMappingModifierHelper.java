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

    /**
     * Returns whether an incoming key press activates a key mapping.
     * <p>
     * The key press must match the mapping's bound key, the conflict context must be
     * active, and the mapping's {@link KeyModifier} must be active.
     * <p>
     * {@link KeyModifier#NONE NONE} is context-sensitive. When
     * {@link IKeyConflictContext#requiresExactKeyModifierNone()} is true, a mapping with
     * {@link KeyModifier#NONE NONE} only matches when no modifier keys are held. When it is false,
     * {@link KeyModifier#NONE NONE} matches even when irrelevant modifiers are held.
     * <p>
     * Examples:
     * <ul>
     * <li>In {@link KeyConflictContext#GUI}, a mapping bound to {@code A} with {@link KeyModifier#NONE NONE}
     * matches {@code A} only when the user is not holding {@link KeyModifier#SHIFT Shift},
     * {@link KeyModifier#CONTROL Control}, {@link KeyModifier#CONTROL_OR_COMMAND Command}, or
     * {@link KeyModifier#ALT Alt}, because GUI contexts require exact {@link KeyModifier#NONE NONE} matching
     * ({@link IKeyConflictContext#requiresExactKeyModifierNone()} is true).</li>
     * <li>In {@link KeyConflictContext#IN_GAME}, the same {@code A} mapping can still match while
     * {@link KeyModifier#SHIFT Shift}, {@link KeyModifier#CONTROL Control},
     * {@link KeyModifier#CONTROL_OR_COMMAND Command}, or {@link KeyModifier#ALT Alt} is held, because
     * in-game contexts treat {@link KeyModifier#NONE NONE} as no additional modifier requirement
     * ({@link IKeyConflictContext#requiresExactKeyModifierNone()} is false).</li>
     * <li>A mapping bound to {@code A} with {@link KeyModifier#SHIFT Shift} only matches
     * {@code A} while {@link KeyModifier#SHIFT Shift} is active. Additional held modifiers, such as
     * {@link KeyModifier#CONTROL Control}, do not prevent it from matching.</li>
     * <li>A mapping bound directly to the left {@link KeyModifier#SHIFT Shift} key with {@link KeyModifier#NONE NONE}
     * matches the left {@link KeyModifier#SHIFT Shift} key, even in {@link KeyConflictContext#GUI}. Here,
     * {@link KeyModifier#NONE NONE} means the mapping has no additional modifier requirement; the bound key
     * itself may still be {@link KeyModifier#SHIFT Shift}, {@link KeyModifier#CONTROL Control},
     * {@link KeyModifier#CONTROL_OR_COMMAND Command}, or {@link KeyModifier#ALT Alt}.</li>
     * </ul>
     *
     * @param keyCode         the incoming key press to test
     * @param boundKey        the mapping's configured key
     * @param conflictContext the mapping's conflict context
     * @param keyModifier     the mapping's modifier requirement
     * @return {@code true} if the key press matches the bound key, the conflict context is active, and the
     *         modifier requirement is active
     * @see IKeyConflictContext#requiresExactKeyModifierNone()
     * @see KeyModifier#isActive(IKeyConflictContext)
     * @see #isActiveAndMatches(InputConstants.Key, InputConstants.Key, IKeyConflictContext, boolean)
     */
    public static boolean isActiveAndMatches(InputConstants.Key keyCode, InputConstants.Key boundKey, IKeyConflictContext conflictContext, KeyModifier keyModifier) {
        return isActiveAndMatches(keyCode, boundKey, conflictContext, isModifierActive(conflictContext, keyModifier, boundKey));
    }

    /**
     * Variant of {@link #isActiveAndMatches(InputConstants.Key, InputConstants.Key, IKeyConflictContext, KeyModifier)}
     * for callers that have already checked whether the mapping's modifier
     * requirement is active.
     *
     * @param keyCode         the incoming key press to test
     * @param boundKey        the mapping's configured key
     * @param conflictContext the mapping's conflict context
     * @param modifierActive  whether the mapping's modifier requirement is active
     * @return {@code true} if the key press matches the bound key, the conflict context is active, and
     *         {@code modifierActive} is true
     * @see #isActiveAndMatches(InputConstants.Key, InputConstants.Key, IKeyConflictContext, KeyModifier)
     */
    public static boolean isActiveAndMatches(InputConstants.Key keyCode, InputConstants.Key boundKey, IKeyConflictContext conflictContext, boolean modifierActive) {
        return keyCode != InputConstants.UNKNOWN && keyCode.equals(boundKey) && conflictContext.isActive() && modifierActive;
    }

    /**
     * Returns whether two key mappings conflict in the controls screen.
     * <p>
     * Two mappings conflict when their contexts conflict and either:
     * <ul>
     * <li>one mapping's bound key is the other mapping's modifier, such as
     * the left {@link KeyModifier#SHIFT Shift} key conflicting with an {@code A} mapping that uses
     * {@link KeyModifier#SHIFT Shift}, or</li>
     * <li>both mappings use the same bound key and their modifiers conflict, such as
     * two {@code A} mappings that both use {@link KeyModifier#SHIFT Shift}.</li>
     * </ul>
     * A same-key bare mapping only conflicts with a modified mapping when the bare
     * mapping's context does not require exact {@link KeyModifier#NONE NONE} matching.
     * For example, {@code A} conflicts with an {@code A} mapping that uses
     * {@link KeyModifier#SHIFT Shift} when {@link IKeyConflictContext#requiresExactKeyModifierNone()}
     * returns false.
     * When it returns true, the same two mappings can coexist because bare {@code A}
     * does not activate while {@link KeyModifier#SHIFT Shift} is held.
     *
     * @param first  the first key mapping to compare
     * @param second the second key mapping to compare
     * @return {@code true} if the two key mappings conflict
     * @see KeyMapping#same(KeyMapping)
     * @see #hasKeyModifierConflict(KeyMapping, KeyMapping)
     */
    public static boolean hasKeyMappingConflict(KeyMapping first, KeyMapping second) {
        return hasKeyMappingConflict(
                first.getKey(), first.getKeyConflictContext(), first.getKeyModifier(),
                second.getKey(), second.getKeyConflictContext(), second.getKeyModifier());
    }

    /**
     * Returns whether either mapping uses the other mapping's bound key as its
     * modifier key.
     * <p>
     * For example, a bare mapping bound directly to the left {@link KeyModifier#SHIFT Shift} key conflicts
     * with an {@code A} mapping that uses {@link KeyModifier#SHIFT Shift} when their contexts conflict.
     * This method does not report same-key conflicts like two {@code A} mappings; use
     * {@link #hasKeyMappingConflict(KeyMapping, KeyMapping)} for the full conflict
     * check used by {@link KeyMapping#same(KeyMapping)}.
     *
     * @param first  the first key mapping to compare
     * @param second the second key mapping to compare
     * @return {@code true} if either mapping uses the other mapping's bound key as its modifier
     * @see #hasKeyMappingConflict(KeyMapping, KeyMapping)
     */
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
