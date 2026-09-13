/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import net.neoforged.neoforge.client.extensions.KeyMappingModifierHelper;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.lwjgl.glfw.GLFW;

class KeyMappingLookupTest {
    private static final AtomicInteger NEXT_MAPPING_ID = new AtomicInteger();
    private static final InputConstants.Key A = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_A);
    private static final InputConstants.Key LEFT_SHIFT = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT);
    private static final InputConstants.Key LEFT_ALT = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_ALT);
    private static final IKeyConflictContext EXACT_NONE_CONTEXT = new TestKeyConflictContext(true, true);
    private static final IKeyConflictContext NON_EXACT_NONE_CONTEXT = new TestKeyConflictContext(true, false);
    private static final IKeyConflictContext INACTIVE_EXACT_NONE_CONTEXT = new TestKeyConflictContext(false, true);

    @Test
    void bareKeyMatchesWhenNoModifiersAreHeld() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.NONE, A);

        // When the user presses A by itself, a keybinding assigned to plain A
        // should become pressed.
        Assertions.assertEquals(List.of(bareA), press(lookup, keyState, A));
        Assertions.assertTrue(bareA.isDown());
    }

    @Test
    void bareKeyCanStillBePressedWhenUnusedModifierIsHeldInNonExactNoneContext() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareA = put(lookup, keyState, NON_EXACT_NONE_CONTEXT, KeyModifier.NONE, A);
        keyState.press(LEFT_SHIFT);

        // Some contexts allow a plain A binding to work even while an unrelated
        // modifier is held. In those contexts, holding Shift should not stop A
        // from becoming pressed.
        Assertions.assertEquals(List.of(bareA), press(lookup, keyState, A));
        Assertions.assertTrue(bareA.isDown());
    }

    @Test
    void pressingModifierKeyDoesNotPressDifferentBareKey() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.NONE, A);

        // (sanity check) A keybinding assigned to plain A should not become
        // pressed when Shift is pressed.
        Assertions.assertEquals(List.of(), press(lookup, keyState, LEFT_SHIFT));
        Assertions.assertFalse(bareA.wasPressDownCalled());
        Assertions.assertFalse(bareA.isDown());
    }

    @Test
    void bareKeyDoesNotPressWhenPressedWithModifierInExactNoneContext() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.NONE, A);
        keyState.press(LEFT_SHIFT);

        // With Shift already held, pressing A is the Shift+A input. GUI-style
        // contexts keep that separate from plain A, so the plain A keybinding
        // should not become pressed.
        Assertions.assertEquals(List.of(), press(lookup, keyState, A));
        Assertions.assertFalse(bareA.wasPressDownCalled());
        Assertions.assertFalse(bareA.isDown());
    }

    @Test
    void modifiedBindingTakesPriorityOverBareBindingWhenModifierIsHeld() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareA = put(lookup, keyState, NON_EXACT_NONE_CONTEXT, KeyModifier.NONE, A);
        TestKeyMapping shiftA = put(lookup, keyState, NON_EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A);
        keyState.press(LEFT_SHIFT);

        // When both plain A and Shift+A are registered, pressing Shift+A should
        // use the more specific Shift+A binding instead of also pressing plain A.
        Assertions.assertEquals(List.of(shiftA), press(lookup, keyState, A));
        Assertions.assertFalse(bareA.wasPressDownCalled());
        Assertions.assertTrue(shiftA.isDown());
    }

    @Test
    void modifiedBindingMatchesWithAdditionalUnrelatedModifierHeld() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping shiftA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A);
        keyState.press(LEFT_SHIFT);
        keyState.press(LEFT_ALT);

        // A Shift+A binding requires Shift to be held, but it does not require
        // Shift to be the only held modifier. Holding Alt too should not stop
        // Shift+A from becoming pressed.
        Assertions.assertEquals(List.of(shiftA), press(lookup, keyState, A));
        Assertions.assertTrue(shiftA.isDown());
    }

    @Test
    void multipleModifiedBindingsCanBePressedWhenTheirModifiersAreHeld() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping shiftA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A);
        TestKeyMapping altA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.ALT, A);
        keyState.press(LEFT_SHIFT);
        keyState.press(LEFT_ALT);

        // If the user holds both Shift and Alt, then pressing A satisfies both
        // Shift+A and Alt+A. Both keybindings should become pressed.
        Assertions.assertEquals(List.of(shiftA, altA), press(lookup, keyState, A));
        Assertions.assertTrue(shiftA.isDown());
        Assertions.assertTrue(altA.isDown());
    }

    @Test
    void pressingModifierKeyDoesNotTriggerChordForUnpressedNonModifierKey() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareShift = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.NONE, LEFT_SHIFT);
        TestKeyMapping shiftA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A);

        // Pressing Shift by itself should press a keybinding assigned directly to
        // Shift. It should not also press Shift+A until A is held too.
        Assertions.assertEquals(List.of(bareShift), press(lookup, keyState, LEFT_SHIFT));
        Assertions.assertTrue(bareShift.isDown());
        Assertions.assertFalse(shiftA.wasPressDownCalled());
    }

    @Test
    void pressingModifierKeyCanTriggerChordBoundToHeldModifierKey() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping shiftAlt = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, LEFT_ALT);
        keyState.press(LEFT_ALT);

        // If Alt is already held, pressing Shift completes the Shift+Alt
        // combination. The Shift+Alt keybinding should become pressed at that
        // point.
        Assertions.assertEquals(List.of(shiftAlt), press(lookup, keyState, LEFT_SHIFT));
        Assertions.assertTrue(shiftAlt.isDown());
    }

    @Test
    void inactiveBindingDoesNotPressWhenItsKeyIsPressed() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareA = put(lookup, keyState, INACTIVE_EXACT_NONE_CONTEXT, KeyModifier.NONE, A);

        // If a keybinding is not active in the current screen or game state,
        // pressing its key should not make it pressed.
        Assertions.assertEquals(List.of(), press(lookup, keyState, A));
        Assertions.assertFalse(bareA.wasPressDownCalled());
    }

    @Test
    void toggleModifiedBindingStaysDownWhenModifierIsReleased() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestToggleKeyMapping shiftA = putToggle(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A, true);
        keyState.press(LEFT_SHIFT);

        Assertions.assertEquals(List.of(shiftA), press(lookup, keyState, A));
        Assertions.assertTrue(shiftA.isDown());

        // Releasing Shift should still send the release to Shift+A. Because this
        // keybinding is a toggle, that release should not turn it off.
        Assertions.assertEquals(List.of(shiftA), release(lookup, keyState, LEFT_SHIFT));
        Assertions.assertTrue(shiftA.wasReleaseDownCalled());
        Assertions.assertTrue(shiftA.isDown());
    }

    @Test
    void toggleModifiedBindingTogglesOffOnNextPressAfterKeyRelease() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestToggleKeyMapping shiftA = putToggle(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A, true);
        keyState.press(LEFT_SHIFT);

        Assertions.assertEquals(List.of(shiftA), press(lookup, keyState, A));
        Assertions.assertTrue(shiftA.isDown());

        // Releasing the key should not turn off a toggle binding. The next
        // Shift+A press should toggle it off.
        Assertions.assertEquals(List.of(shiftA), release(lookup, keyState, A));
        Assertions.assertTrue(shiftA.wasReleaseDownCalled());
        Assertions.assertTrue(shiftA.isDown());
        Assertions.assertEquals(List.of(shiftA), press(lookup, keyState, A));
        Assertions.assertTrue(shiftA.wasPressDownCalled());
        Assertions.assertFalse(shiftA.isDown());
    }

    @ParameterizedTest
    @MethodSource("activeContexts")
    void bareModifierAndModifiedKeyCanBothBeDownInLookupPressSequence(IKeyConflictContext keyConflictContext) {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareShift = put(lookup, keyState, keyConflictContext, KeyModifier.NONE, LEFT_SHIFT);
        TestKeyMapping shiftA = put(lookup, keyState, keyConflictContext, KeyModifier.SHIFT, A);

        // A user can bind one action to Shift and another to Shift+A. Pressing
        // Shift should first make the plain Shift action pressed in both
        // GUI-style and non-GUI-style contexts.
        Assertions.assertEquals(List.of(bareShift), press(lookup, keyState, LEFT_SHIFT));
        Assertions.assertTrue(bareShift.isDown());
        Assertions.assertFalse(shiftA.isDown());

        // While Shift remains held, pressing A should also make the Shift+A
        // action pressed. The plain Shift action should stay pressed too.
        Assertions.assertEquals(List.of(shiftA), press(lookup, keyState, A));
        Assertions.assertTrue(bareShift.isDown());
        Assertions.assertTrue(shiftA.isDown());

        // Releasing Shift should release both actions. A is still held, but the
        // Shift+A action can no longer be considered pressed once Shift is up.
        Assertions.assertEquals(List.of(bareShift, shiftA), release(lookup, keyState, LEFT_SHIFT));
        Assertions.assertFalse(keyState.isDown(LEFT_SHIFT));
        Assertions.assertTrue(keyState.isDown(A));
        Assertions.assertTrue(bareShift.wasReleaseDownCalled());
        Assertions.assertTrue(shiftA.wasReleaseDownCalled());
        Assertions.assertFalse(bareShift.isDown());
        Assertions.assertFalse(shiftA.isDown());
    }

    @ParameterizedTest
    @MethodSource("activeContexts")
    void releasingModifierKeyClearsModifiedMappingsThatAreNoLongerActive(IKeyConflictContext keyConflictContext) {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareShift = put(lookup, keyState, keyConflictContext, KeyModifier.NONE, LEFT_SHIFT);
        TestKeyMapping shiftA = put(lookup, keyState, keyConflictContext, KeyModifier.SHIFT, A);

        keyState.press(LEFT_SHIFT);
        keyState.press(A);
        bareShift.setDown(true);
        shiftA.setDown(true);
        shiftA.setKeyConflictContext(new TestKeyConflictContext(false, keyConflictContext.requiresExactKeyModifierNone()));

        // A keybinding can become inactive before the key-up event arrives, such
        // as when the screen changes while the key is held. The release still
        // needs to reach it so it does not stay stuck down.
        Assertions.assertEquals(List.of(bareShift, shiftA), release(lookup, keyState, LEFT_SHIFT));
        Assertions.assertTrue(bareShift.wasReleaseDownCalled());
        Assertions.assertTrue(shiftA.wasReleaseDownCalled());
    }

    @Test
    void releasingNormalKeyClearsAllMappingsBoundToThatKey() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.NONE, A);
        TestKeyMapping shiftA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A);
        TestKeyMapping altA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.ALT, A);
        bareA.setDown(true);
        shiftA.setDown(true);
        altA.setDown(true);

        // Releasing A should release every action assigned to A, including
        // modified actions such as Shift+A and Alt+A.
        assertContainsOnly(release(lookup, keyState, A), bareA, shiftA, altA);
        Assertions.assertTrue(bareA.wasReleaseDownCalled());
        Assertions.assertTrue(shiftA.wasReleaseDownCalled());
        Assertions.assertTrue(altA.wasReleaseDownCalled());
    }

    @Test
    void releasingModifierKeyIgnoresUnknownModifiedBindings() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareShift = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.NONE, LEFT_SHIFT);
        TestKeyMapping shiftA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A);
        TestKeyMapping shiftUnknown = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, InputConstants.UNKNOWN);
        bareShift.setDown(true);
        shiftA.setDown(true);
        shiftUnknown.setDown(true);

        // Unknown-key bindings are placeholders for unbound actions. Releasing
        // Shift should release real Shift-dependent bindings, but not an unbound
        // placeholder.
        Assertions.assertEquals(List.of(bareShift, shiftA), release(lookup, keyState, LEFT_SHIFT));
        Assertions.assertTrue(bareShift.wasReleaseDownCalled());
        Assertions.assertTrue(shiftA.wasReleaseDownCalled());
        Assertions.assertFalse(shiftUnknown.wasReleaseDownCalled());
    }

    @Test
    void releasingModifierKeyClearsDirectModifierBindingsButNotOtherModifierBuckets() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping altShift = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.ALT, LEFT_SHIFT);
        TestKeyMapping shiftA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A);
        TestKeyMapping altA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.ALT, A);
        altShift.setDown(true);
        shiftA.setDown(true);
        altA.setDown(true);

        // Releasing Shift should release actions assigned directly to Shift,
        // including combinations like Alt+Shift. It should also release actions
        // like Shift+A, but it must not release unrelated combinations like Alt+A.
        Assertions.assertEquals(List.of(altShift, shiftA), release(lookup, keyState, LEFT_SHIFT));
        Assertions.assertTrue(altShift.wasReleaseDownCalled());
        Assertions.assertTrue(shiftA.wasReleaseDownCalled());
        Assertions.assertFalse(altA.wasReleaseDownCalled());
    }

    @Test
    void removedBindingDoesNotPressWhenItsKeysArePressed() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareA = put(lookup, keyState, NON_EXACT_NONE_CONTEXT, KeyModifier.NONE, A);
        TestKeyMapping shiftA = put(lookup, keyState, NON_EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A);
        lookup.remove(shiftA);
        keyState.press(LEFT_SHIFT);

        // After the Shift+A binding is removed, pressing Shift+A should no longer
        // press that action. In this context, the remaining plain A binding can
        // still be pressed while Shift is held.
        Assertions.assertEquals(List.of(bareA), press(lookup, keyState, A));
        Assertions.assertTrue(bareA.isDown());
        Assertions.assertFalse(shiftA.wasPressDownCalled());
    }

    @Test
    void clearRemovesAllMappings() {
        KeyState keyState = new KeyState();
        KeyMappingLookup lookup = new TestKeyMappingLookup(keyState);
        TestKeyMapping bareA = put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.NONE, A);
        put(lookup, keyState, EXACT_NONE_CONTEXT, KeyModifier.SHIFT, A);
        lookup.clear();

        // After all bindings are cleared, pressing A should not make any previous
        // A binding pressed.
        Assertions.assertEquals(List.of(), press(lookup, keyState, A));
        Assertions.assertFalse(bareA.wasPressDownCalled());
    }

    private static TestKeyMapping put(KeyMappingLookup lookup, KeyState keyState, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key key) {
        TestKeyMapping keyMapping = keyMapping(keyState, keyConflictContext, keyModifier, key);
        lookup.put(key, keyMapping);
        return keyMapping;
    }

    private static TestKeyMapping keyMapping(KeyState keyState, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key key) {
        return new TestKeyMapping("key.neoforge.test.key_mapping_lookup." + NEXT_MAPPING_ID.incrementAndGet(), keyState, keyConflictContext, keyModifier, key);
    }

    private static TestToggleKeyMapping putToggle(KeyMappingLookup lookup, KeyState keyState, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key key, boolean needsToggle) {
        TestToggleKeyMapping keyMapping = new TestToggleKeyMapping("key.neoforge.test.key_mapping_lookup." + NEXT_MAPPING_ID.incrementAndGet(), keyState, keyConflictContext, keyModifier, key, needsToggle);
        lookup.put(key, keyMapping);
        return keyMapping;
    }

    private static List<KeyMapping> press(KeyMappingLookup lookup, KeyState keyState, InputConstants.Key key) {
        keyState.press(key);
        List<KeyMapping> mappings = lookup.getAll(key, false);
        mappings.forEach(mapping -> mapping.setDown(true));
        return mappings;
    }

    private static List<KeyMapping> release(KeyMappingLookup lookup, KeyState keyState, InputConstants.Key key) {
        keyState.release(key);
        List<KeyMapping> mappings = lookup.getAll(key, true);
        mappings.forEach(mapping -> mapping.setDown(false));
        return mappings;
    }

    private static void assertContainsOnly(List<KeyMapping> actual, KeyMapping... expected) {
        Assertions.assertEquals(expected.length, actual.size());
        Assertions.assertEquals(new HashSet<>(Arrays.asList(expected)), new HashSet<>(actual));
    }

    private static Stream<IKeyConflictContext> activeContexts() {
        return Stream.of(EXACT_NONE_CONTEXT, NON_EXACT_NONE_CONTEXT);
    }

    // Normal KeyMapping active checks read the live Minecraft/GLFW key-state
    // table through KeyModifier. These tests run without a client window, so the
    // test mappings use KeyState only for the modifier/key-state portion.
    private static boolean isActiveAndMatches(KeyMapping keyMapping, KeyState keyState, InputConstants.Key keyCode) {
        return KeyMappingModifierHelper.isActiveAndMatches(keyCode, keyMapping.getKey(), keyMapping.getKeyConflictContext(), isModifierActive(keyMapping, keyState));
    }

    private static boolean isConflictContextAndModifierActive(KeyMapping keyMapping, KeyState keyState) {
        return keyMapping.getKeyConflictContext().isActive() && isModifierActive(keyMapping, keyState);
    }

    private static boolean isModifierActive(KeyMapping keyMapping, KeyState keyState) {
        KeyModifier keyModifier = keyMapping.getKeyModifier();
        if (keyModifier != KeyModifier.NONE) {
            return keyState.isModifierDown(keyModifier);
        }

        if (KeyModifier.isKeyCodeModifier(keyMapping.getKey())) {
            return true;
        }
        return !keyMapping.getKeyConflictContext().requiresExactKeyModifierNone() || keyState.activeModifiers().isEmpty();
    }

    private static final class KeyState {
        private final Set<InputConstants.Key> downKeys = new HashSet<>();

        private void press(InputConstants.Key key) {
            downKeys.add(key);
        }

        private void release(InputConstants.Key key) {
            downKeys.remove(key);
        }

        private boolean isDown(InputConstants.Key key) {
            return downKeys.contains(key);
        }

        private boolean isModifierDown(KeyModifier modifier) {
            for (InputConstants.Key key : modifier.codes()) {
                if (downKeys.contains(key)) {
                    return true;
                }
            }
            return false;
        }

        private List<KeyModifier> activeModifiers() {
            List<KeyModifier> activeModifiers = new ArrayList<>();
            for (KeyModifier modifier : KeyModifier.MODIFIER_VALUES) {
                for (InputConstants.Key key : modifier.codes()) {
                    if (downKeys.contains(key)) {
                        activeModifiers.add(modifier);
                        break;
                    }
                }
            }
            return activeModifiers;
        }
    }

    private static final class TestKeyMappingLookup extends KeyMappingLookup {
        private final KeyState keyState;

        private TestKeyMappingLookup(KeyState keyState) {
            this.keyState = keyState;
        }

        @Override
        protected List<KeyModifier> getActiveModifiers() {
            return keyState.activeModifiers();
        }

        @Override
        protected boolean isKeyDown(InputConstants.Key key) {
            return keyState.isDown(key);
        }
    }

    /**
     * KeyMapping test double that keeps the real storage and down-state behavior,
     * but replaces live client modifier checks with the test's KeyState.
     */
    private static final class TestKeyMapping extends KeyMapping {
        private final KeyState keyState;
        private final DownCallTracker downCallTracker = new DownCallTracker();

        private TestKeyMapping(String name, KeyState keyState, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key key) {
            super(name, keyConflictContext, keyModifier, key, KeyMapping.Category.MISC);
            this.keyState = keyState;
        }

        @Override
        public void setDown(boolean down) {
            this.downCallTracker.record(down);
            super.setDown(down);
        }

        @Override
        public boolean isActiveAndMatches(InputConstants.Key keyCode) {
            return KeyMappingLookupTest.isActiveAndMatches(this, this.keyState, keyCode);
        }

        @Override
        public boolean isConflictContextAndModifierActive() {
            return KeyMappingLookupTest.isConflictContextAndModifierActive(this, this.keyState);
        }

        private boolean wasPressDownCalled() {
            return this.downCallTracker.wasPressDownCalled();
        }

        private boolean wasReleaseDownCalled() {
            return this.downCallTracker.wasReleaseDownCalled();
        }
    }

    /**
     * ToggleKeyMapping variant of the same test double. This keeps
     * ToggleKeyMapping's setDown/isDown behavior while avoiding live client
     * modifier checks.
     */
    private static final class TestToggleKeyMapping extends ToggleKeyMapping {
        private final KeyState keyState;
        private final DownCallTracker downCallTracker = new DownCallTracker();

        private TestToggleKeyMapping(String name, KeyState keyState, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key key, boolean needsToggle) {
            super(name, key.getType(), key.getValue(), KeyMapping.Category.MISC, () -> needsToggle, true);
            this.keyState = keyState;
            setKeyConflictContext(keyConflictContext);
            setKeyModifierAndCode(keyModifier, key);
        }

        @Override
        public void setDown(boolean down) {
            this.downCallTracker.record(down);
            super.setDown(down);
        }

        @Override
        public boolean isActiveAndMatches(InputConstants.Key keyCode) {
            return KeyMappingLookupTest.isActiveAndMatches(this, this.keyState, keyCode);
        }

        @Override
        public boolean isConflictContextAndModifierActive() {
            return KeyMappingLookupTest.isConflictContextAndModifierActive(this, this.keyState);
        }

        private boolean wasPressDownCalled() {
            return this.downCallTracker.wasPressDownCalled();
        }

        private boolean wasReleaseDownCalled() {
            return this.downCallTracker.wasReleaseDownCalled();
        }
    }

    private static final class DownCallTracker {
        private boolean pressDownCalled;
        private boolean releaseDownCalled;

        private void record(boolean down) {
            if (down) {
                this.pressDownCalled = true;
            } else {
                this.releaseDownCalled = true;
            }
        }

        private boolean wasPressDownCalled() {
            return this.pressDownCalled;
        }

        private boolean wasReleaseDownCalled() {
            return this.releaseDownCalled;
        }
    }

    private record TestKeyConflictContext(boolean active, boolean requiresExactKeyModifierNone) implements IKeyConflictContext {
        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return true;
        }
    }
}
