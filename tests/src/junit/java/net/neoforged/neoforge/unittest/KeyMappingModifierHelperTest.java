/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static net.neoforged.neoforge.client.extensions.KeyMappingModifierHelper.hasKeyMappingConflict;
import static net.neoforged.neoforge.client.extensions.KeyMappingModifierHelper.isActiveAndMatches;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.lwjgl.glfw.GLFW;

class KeyMappingModifierHelperTest {
    private static final AtomicInteger NEXT_MAPPING_ID = new AtomicInteger();
    private static final InputConstants.Key A = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_A);
    private static final InputConstants.Key B = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_B);
    private static final InputConstants.Key F3 = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F3);
    private static final InputConstants.Key L = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_L);
    private static final InputConstants.Key P = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_P);
    private static final InputConstants.Key T = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_T);
    private static final InputConstants.Key LEFT_SHIFT = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT);
    private static final InputConstants.Key MOUSE_3 = InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
    private static final InputConstants.Key ACTIVE_CONTEXT_KEY = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_C);
    private static final IKeyConflictContext ACTIVE_GUI_CONTEXT = new TestKeyConflictContext("gui", true, false);
    private static final IKeyConflictContext INACTIVE_GUI_CONTEXT = new TestKeyConflictContext("gui", false, false);
    private static final IKeyConflictContext ACTIVE_IN_GAME_LIKE_CONTEXT = new TestKeyConflictContext("in_game_like", true, true, false, false);
    private static final IKeyConflictContext INACTIVE_IN_GAME_LIKE_CONTEXT = new TestKeyConflictContext("in_game_like", false, true, false, false);

    @Test
    void normalBareKeyWithNoExtraModifierIsActive() {
        Assertions.assertTrue(isActiveAndMatches(A, A, ACTIVE_GUI_CONTEXT, true));
    }

    @Test
    void normalBareKeyDoesNotMatchDifferentKey() {
        Assertions.assertFalse(isActiveAndMatches(B, A, ACTIVE_GUI_CONTEXT, true));
    }

    @Test
    void unknownKeyNeverMatches() {
        Assertions.assertFalse(isActiveAndMatches(InputConstants.UNKNOWN, A, ACTIVE_GUI_CONTEXT, true));
    }

    @Test
    void keyMappingBoundToNormalKeyCanBeDownWhenContextAllowsNoModifier() {
        var keyMapping = keyMapping(ACTIVE_IN_GAME_LIKE_CONTEXT, KeyModifier.NONE, ACTIVE_CONTEXT_KEY);
        keyMapping.setDown(true);

        Assertions.assertTrue(keyMapping.isActiveAndMatches(ACTIVE_CONTEXT_KEY));
        Assertions.assertFalse(keyMapping.isActiveAndMatches(A));
        Assertions.assertTrue(keyMapping.isConflictContextAndModifierActive());
        Assertions.assertTrue(keyMapping.isDown());
    }

    @Test
    void keyMappingBoundToNormalKeyStillRequiresActiveContext() {
        var keyMapping = keyMapping(INACTIVE_IN_GAME_LIKE_CONTEXT, KeyModifier.NONE, ACTIVE_CONTEXT_KEY);
        keyMapping.setDown(true);

        Assertions.assertFalse(keyMapping.isActiveAndMatches(ACTIVE_CONTEXT_KEY));
        Assertions.assertFalse(keyMapping.isConflictContextAndModifierActive());
        Assertions.assertFalse(keyMapping.isDown());
    }

    @ParameterizedTest
    @MethodSource("modifierKeys")
    void bareModifierKeyWithNoSeparateModifierIsActive(InputConstants.Key key) {
        Assertions.assertTrue(isActiveAndMatches(key, key, ACTIVE_GUI_CONTEXT, KeyModifier.NONE));

        var keyMapping = new KeyMapping("key.neoforge.test.bare_modifier." + key.getValue(), ACTIVE_GUI_CONTEXT, KeyModifier.NONE, key, KeyMapping.Category.MISC);
        keyMapping.setDown(true);

        Assertions.assertTrue(keyMapping.isActiveAndMatches(key));
        Assertions.assertTrue(keyMapping.isConflictContextAndModifierActive());
        Assertions.assertTrue(keyMapping.isDown());
    }

    @ParameterizedTest
    @MethodSource("modifierKeys")
    void bareModifierKeyStillRequiresActiveConflictContext(InputConstants.Key key) {
        Assertions.assertFalse(isActiveAndMatches(key, key, INACTIVE_GUI_CONTEXT, KeyModifier.NONE));

        var keyMapping = new KeyMapping("key.neoforge.test.inactive_bare_modifier." + key.getValue(), INACTIVE_GUI_CONTEXT, KeyModifier.NONE, key, KeyMapping.Category.MISC);
        keyMapping.setDown(true);

        Assertions.assertFalse(keyMapping.isConflictContextAndModifierActive());
        Assertions.assertFalse(keyMapping.isDown());
    }

    @ParameterizedTest
    @MethodSource("modifierKeys")
    void bareModifierKeyStillRequiresMatchingKey(InputConstants.Key key) {
        Assertions.assertFalse(isActiveAndMatches(A, key, ACTIVE_GUI_CONTEXT, KeyModifier.NONE));
    }

    @Test
    void bareNonModifierKeyIsInactiveWhenExactNoneMatchingFails() {
        Assertions.assertFalse(isActiveAndMatches(A, A, ACTIVE_GUI_CONTEXT, false));
    }

    @Test
    void bareNonModifierKeyUsesNormalNoneModifierState() {
        Assertions.assertTrue(isActiveAndMatches(A, A, ACTIVE_GUI_CONTEXT, true));
    }

    @ParameterizedTest
    @MethodSource("modifierChordKeys")
    void modifiedKeyRequiresModifierAndKey(InputConstants.Key modifierKey) {
        Assertions.assertFalse(isActiveAndMatches(A, A, ACTIVE_GUI_CONTEXT, false));
        Assertions.assertFalse(isActiveAndMatches(modifierKey, A, ACTIVE_GUI_CONTEXT, true));
        Assertions.assertTrue(isActiveAndMatches(A, A, ACTIVE_GUI_CONTEXT, true));
    }

    @Test
    void modifiedKeyCanMatchWhenAdditionalModifierKeysAreHeld() {
        Assertions.assertTrue(isActiveAndMatches(A, A, ACTIVE_GUI_CONTEXT, true));
    }

    @Test
    void modifiedKeyStillRequiresActiveContext() {
        Assertions.assertFalse(isActiveAndMatches(A, A, INACTIVE_GUI_CONTEXT, true));
    }

    @Test
    void sameKeyWithConflictingContextsAndSameModifierConflicts() {
        var first = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.NONE, A);
        var second = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.NONE, A);

        assertMappingConflict(first, second);
    }

    @Test
    void sameKeyConflictIsNotAKeyModifierConflict() {
        var first = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.NONE, A);
        var second = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.NONE, A);

        Assertions.assertFalse(first.hasKeyModifierConflict(second));
        Assertions.assertFalse(second.hasKeyModifierConflict(first));
        assertMappingConflict(first, second);
    }

    @Test
    void sameKeyWithNonConflictingContextsDoesNotConflict() {
        var first = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.NONE, A);
        var second = keyMapping(new TestKeyConflictContext("game", true, false), KeyModifier.NONE, A);

        assertNoMappingConflict(first, second);
    }

    @Test
    void differentKeysWithConflictingContextsDoNotConflict() {
        var first = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.NONE, A);
        var second = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.NONE, B);

        assertNoMappingConflict(first, second);
        Assertions.assertFalse(first.hasKeyModifierConflict(second));
        Assertions.assertFalse(second.hasKeyModifierConflict(first));
    }

    @Test
    void sameKeyWithSameModifierConflictsInGuiContext() {
        var first = keyMapping(KeyConflictContext.GUI, KeyModifier.SHIFT, A);
        var second = keyMapping(KeyConflictContext.GUI, KeyModifier.SHIFT, A);

        Assertions.assertFalse(first.hasKeyModifierConflict(second));
        Assertions.assertFalse(second.hasKeyModifierConflict(first));
        assertMappingConflict(first, second);
    }

    @Test
    void sameKeyWithDifferentModifiersDoesNotConflictInGuiContext() {
        var shiftA = keyMapping(KeyConflictContext.GUI, KeyModifier.SHIFT, A);
        var controlA = keyMapping(KeyConflictContext.GUI, KeyModifier.CONTROL, A);

        assertNoMappingConflict(shiftA, controlA);
    }

    @Test
    void sameKeyWithBareAndModifiedBindingsDoesNotConflictInGuiContext() {
        var bareA = keyMapping(KeyConflictContext.GUI, KeyModifier.NONE, A);
        var shiftA = keyMapping(KeyConflictContext.GUI, KeyModifier.SHIFT, A);

        assertNoMappingConflict(bareA, shiftA);
    }

    @Test
    void sameKeyWithBareAndModifiedBindingsConflictsInInGameContext() {
        var bareA = keyMapping(KeyConflictContext.IN_GAME, KeyModifier.NONE, A);
        var shiftA = keyMapping(KeyConflictContext.IN_GAME, KeyModifier.SHIFT, A);

        assertMappingConflict(bareA, shiftA);
    }

    @Test
    void sameKeyWithCustomNonExactKeyModifierNoneContextAndModifiedBindingConflicts() {
        var bareA = keyMapping(new TestKeyConflictContext("custom", true, false, false, false), KeyModifier.NONE, A);
        var shiftA = keyMapping(new TestKeyConflictContext("custom", true, false, false, false), KeyModifier.SHIFT, A);

        assertMappingConflict(bareA, shiftA);
    }

    @Test
    void sameKeyWithCustomExactKeyModifierNoneContextAndModifiedBindingDoesNotConflict() {
        var bareA = keyMapping(new TestKeyConflictContext("custom", true, false, false, true), KeyModifier.NONE, A);
        var shiftA = keyMapping(new TestKeyConflictContext("custom", true, false, false, true), KeyModifier.SHIFT, A);

        assertNoMappingConflict(bareA, shiftA);
    }

    @Test
    void sameKeyWithBareContextRequiringExactKeyModifierNoneAndModifiedGuiBindingsDoesNotConflict() {
        var dualUseA = keyMapping(new TestKeyConflictContext("gui_and_in_game", true, true, true, true), KeyModifier.NONE, A);
        var guiShiftA = keyMapping(KeyConflictContext.GUI, KeyModifier.SHIFT, A);

        assertNoMappingConflict(dualUseA, guiShiftA);
    }

    @Test
    void sameKeyWithBareGuiAndInGameAndModifiedInGameBindingsConflicts() {
        var vanillaDualUseA = keyMapping(KeyConflictContext.GUI_AND_IN_GAME, KeyModifier.NONE, A);
        var inGameShiftA = keyMapping(KeyConflictContext.IN_GAME, KeyModifier.SHIFT, A);

        assertMappingConflict(vanillaDualUseA, inGameShiftA);
    }

    @Test
    void sameKeyWithBareUniversalAndModifiedGuiBindingsConflicts() {
        var universalA = keyMapping(KeyConflictContext.UNIVERSAL, KeyModifier.NONE, A);
        var guiShiftA = keyMapping(KeyConflictContext.GUI, KeyModifier.SHIFT, A);

        assertMappingConflict(universalA, guiShiftA);
    }

    @Test
    void sameKeyWithBareAndModifiedUniversalBindingsConflict() {
        var universalA = keyMapping(KeyConflictContext.UNIVERSAL, KeyModifier.NONE, A);
        var universalShiftA = keyMapping(KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, A);

        assertMappingConflict(universalA, universalShiftA);
    }

    @Test
    void debugModifierKeyDoesNotConflictWithDebugContextBindings() {
        var debugOverlay = keyMapping(KeyConflictContext.UNIVERSAL, KeyModifier.NONE, F3);
        var debugA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, A);
        var debugB = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, B);

        assertNoMappingConflict(debugOverlay, debugA);
        assertNoMappingConflict(debugOverlay, debugB);
        Assertions.assertFalse(debugOverlay.hasKeyModifierConflict(debugA));
        Assertions.assertFalse(debugA.hasKeyModifierConflict(debugOverlay));
    }

    @Test
    void debugContextBindingsOnlyConflictWhenTheirActionKeyMatches() {
        var firstDebugA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, A);
        var secondDebugA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, A);
        var debugB = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, B);

        assertMappingConflict(firstDebugA, secondDebugA);
        assertNoMappingConflict(firstDebugA, debugB);
    }

    @Test
    void debugContextBareAndModifiedBindingsConflictWhenTheirActionKeyMatches() {
        var debugA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, A);
        var debugShiftA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.SHIFT, A);

        assertMappingConflict(debugA, debugShiftA);
    }

    @Test
    void debugContextBareAndModifiedBindingsDoNotConflictWhenTheirActionKeyDiffers() {
        var debugA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, A);
        var debugShiftB = keyMapping(KeyConflictContext.DEBUG, KeyModifier.SHIFT, B);

        assertNoMappingConflict(debugA, debugShiftB);
    }

    @Test
    void debugContextModifiedBindingsWithDifferentModifiersDoNotConflict() {
        var debugShiftA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.SHIFT, A);
        var debugControlA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.CONTROL, A);

        assertNoMappingConflict(debugShiftA, debugControlA);
    }

    @Test
    void debugProfilingDoesNotConflictWithAdvancements() {
        var profiling = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, L);
        var advancements = keyMapping(KeyConflictContext.GUI_AND_IN_GAME, KeyModifier.NONE, L);

        assertNoMappingConflict(profiling, advancements);
    }

    @Test
    void debugReloadResourcePacksDoesNotConflictWithChat() {
        var reloadResourcePacks = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, T);
        var chat = keyMapping(KeyConflictContext.GUI_AND_IN_GAME, KeyModifier.NONE, T);

        assertNoMappingConflict(reloadResourcePacks, chat);
    }

    @Test
    void debugFocusPauseDoesNotConflictWithSocialInteractions() {
        var focusPause = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, P);
        var socialInteractions = keyMapping(KeyConflictContext.GUI_AND_IN_GAME, KeyModifier.NONE, P);

        assertNoMappingConflict(focusPause, socialInteractions);
    }

    @Test
    void universalContextConflictsWithDebugContextWhenKeyAndModifiersOverlap() {
        var universalA = keyMapping(KeyConflictContext.UNIVERSAL, KeyModifier.NONE, A);
        var debugA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, A);

        assertMappingConflict(universalA, debugA);
    }

    @Test
    void spectatorHotbarDoesNotConflictWithPickBlock() {
        var pickBlock = keyMapping(KeyConflictContext.GUI_AND_IN_GAME, KeyModifier.NONE, MOUSE_3);
        var spectatorHotbar = keyMapping(KeyConflictContext.SPECTATOR, KeyModifier.NONE, MOUSE_3);

        assertNoMappingConflict(pickBlock, spectatorHotbar);
    }

    @Test
    void spectatorBindingsConflictWithSameSpectatorActionKey() {
        var firstSpectatorHotbar = keyMapping(KeyConflictContext.SPECTATOR, KeyModifier.NONE, MOUSE_3);
        var secondSpectatorHotbar = keyMapping(KeyConflictContext.SPECTATOR, KeyModifier.NONE, MOUSE_3);

        assertMappingConflict(firstSpectatorHotbar, secondSpectatorHotbar);
    }

    @Test
    void inGameBareKeyDoesNotConflictWithDebugContextBindingWhenActionKeyMatches() {
        var strafeA = keyMapping(KeyConflictContext.IN_GAME, KeyModifier.NONE, A);
        var debugA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, A);

        assertNoMappingConflict(strafeA, debugA);
    }

    @Test
    void inGameBareKeyDoesNotConflictWithDebugContextBindingWhenActionKeyDiffers() {
        var strafeA = keyMapping(KeyConflictContext.IN_GAME, KeyModifier.NONE, A);
        var debugB = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, B);

        assertNoMappingConflict(strafeA, debugB);
    }

    @Test
    void guiAndInGameBareKeyDoesNotConflictWithDebugContextBindingWhenActionKeyMatches() {
        var inventoryA = keyMapping(KeyConflictContext.GUI_AND_IN_GAME, KeyModifier.NONE, A);
        var debugA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, A);

        assertNoMappingConflict(inventoryA, debugA);
    }

    @Test
    void guiBareKeyDoesNotConflictWithDebugContextBinding() {
        var guiA = keyMapping(KeyConflictContext.GUI, KeyModifier.NONE, A);
        var debugA = keyMapping(KeyConflictContext.DEBUG, KeyModifier.NONE, A);

        assertNoMappingConflict(guiA, debugA);
    }

    @Test
    void modifierMatchingBoundKeyIsStoredAsBareKey() {
        var mapping = keyMapping(KeyConflictContext.GUI, KeyModifier.SHIFT, LEFT_SHIFT);

        Assertions.assertEquals(KeyModifier.NONE, mapping.getKeyModifier());
        Assertions.assertEquals(LEFT_SHIFT, mapping.getKey());
    }

    @Test
    void bareModifierKeyConflictsWithModifiedBindingWhenContextsConflict() {
        var bareShift = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.NONE, LEFT_SHIFT);
        var shiftA = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.SHIFT, A);

        Assertions.assertTrue(bareShift.hasKeyModifierConflict(shiftA));
        Assertions.assertTrue(shiftA.hasKeyModifierConflict(bareShift));
        assertMappingConflict(bareShift, shiftA);
    }

    @Test
    void bareModifierKeyDoesNotConflictWithModifiedBindingWhenContextsDoNotConflict() {
        var bareShift = keyMapping(new TestKeyConflictContext("menu", true, false), KeyModifier.NONE, LEFT_SHIFT);
        var shiftA = keyMapping(new TestKeyConflictContext("game", true, false), KeyModifier.SHIFT, A);

        Assertions.assertFalse(bareShift.hasKeyModifierConflict(shiftA));
        Assertions.assertFalse(shiftA.hasKeyModifierConflict(bareShift));
        assertNoMappingConflict(bareShift, shiftA);
    }

    private static Stream<InputConstants.Key> modifierKeys() {
        return Stream.of(
                GLFW.GLFW_KEY_LEFT_SHIFT,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                GLFW.GLFW_KEY_LEFT_CONTROL,
                GLFW.GLFW_KEY_RIGHT_CONTROL,
                GLFW.GLFW_KEY_LEFT_ALT,
                GLFW.GLFW_KEY_RIGHT_ALT)
                .map(InputConstants.Type.KEYSYM::getOrCreate);
    }

    private static Stream<InputConstants.Key> modifierChordKeys() {
        return Stream.of(
                GLFW.GLFW_KEY_LEFT_SHIFT,
                GLFW.GLFW_KEY_LEFT_CONTROL,
                GLFW.GLFW_KEY_LEFT_ALT)
                .map(InputConstants.Type.KEYSYM::getOrCreate);
    }

    private static KeyMapping keyMapping(IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key key) {
        return new KeyMapping("key.neoforge.test.key_mapping_modifier_helper." + NEXT_MAPPING_ID.incrementAndGet(), keyConflictContext, keyModifier, key, KeyMapping.Category.MISC);
    }

    private static void assertMappingConflict(KeyMapping first, KeyMapping second) {
        Assertions.assertTrue(hasKeyMappingConflict(first, second));
        Assertions.assertTrue(hasKeyMappingConflict(second, first));
        Assertions.assertTrue(first.same(second));
        Assertions.assertTrue(second.same(first));
    }

    private static void assertNoMappingConflict(KeyMapping first, KeyMapping second) {
        Assertions.assertFalse(hasKeyMappingConflict(first, second));
        Assertions.assertFalse(hasKeyMappingConflict(second, first));
        Assertions.assertFalse(first.same(second));
        Assertions.assertFalse(second.same(first));
    }

    private record TestKeyConflictContext(
            String group,
            boolean active,
            boolean conflictsWithInGame,
            boolean conflictsWithGui,
            boolean requiresExactKeyModifierNone) implements IKeyConflictContext {
        private TestKeyConflictContext(String group, boolean active, boolean conflictsWithInGame) {
            this(group, active, conflictsWithInGame, false, true);
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            if (conflictsWithInGame && other == KeyConflictContext.IN_GAME) {
                return true;
            }
            if (conflictsWithGui && other == KeyConflictContext.GUI) {
                return true;
            }
            return other instanceof TestKeyConflictContext otherContext && group.equals(otherContext.group);
        }

        @Override
        public boolean requiresExactKeyModifierNone() {
            return requiresExactKeyModifierNone;
        }
    }
}
