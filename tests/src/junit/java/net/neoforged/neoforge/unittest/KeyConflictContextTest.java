/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KeyConflictContextTest {
    @Test
    void guiContextRequiresExactKeyModifierNone() {
        Assertions.assertTrue(KeyConflictContext.GUI.requiresExactKeyModifierNone());
    }

    @Test
    void guiContextOnlyConflictsWithGuiContext() {
        Assertions.assertTrue(KeyConflictContext.GUI.conflicts(KeyConflictContext.GUI));
        Assertions.assertFalse(KeyConflictContext.GUI.conflicts(KeyConflictContext.IN_GAME));
        Assertions.assertFalse(KeyConflictContext.GUI.conflicts(KeyConflictContext.GUI_AND_IN_GAME));
        Assertions.assertFalse(KeyConflictContext.GUI.conflicts(KeyConflictContext.SPECTATOR));
        Assertions.assertFalse(KeyConflictContext.GUI.conflicts(KeyConflictContext.DEBUG));
        Assertions.assertFalse(KeyConflictContext.GUI.conflicts(new TestKeyConflictContext()));
    }

    @Test
    void inGameContextDoesNotRequireExactKeyModifierNone() {
        Assertions.assertFalse(KeyConflictContext.IN_GAME.requiresExactKeyModifierNone());
    }

    @Test
    void inGameContextOnlyConflictsWithInGameContext() {
        Assertions.assertFalse(KeyConflictContext.IN_GAME.conflicts(KeyConflictContext.GUI));
        Assertions.assertTrue(KeyConflictContext.IN_GAME.conflicts(KeyConflictContext.IN_GAME));
        Assertions.assertFalse(KeyConflictContext.IN_GAME.conflicts(KeyConflictContext.GUI_AND_IN_GAME));
        Assertions.assertFalse(KeyConflictContext.IN_GAME.conflicts(KeyConflictContext.SPECTATOR));
        Assertions.assertFalse(KeyConflictContext.IN_GAME.conflicts(KeyConflictContext.DEBUG));
        Assertions.assertFalse(KeyConflictContext.IN_GAME.conflicts(new TestKeyConflictContext()));
    }

    @Test
    void universalContextDoesNotRequireExactKeyModifierNone() {
        Assertions.assertFalse(KeyConflictContext.UNIVERSAL.requiresExactKeyModifierNone());
    }

    @Test
    void universalContextConflictsWithEveryContext() {
        Assertions.assertTrue(KeyConflictContext.UNIVERSAL.conflicts(KeyConflictContext.GUI));
        Assertions.assertTrue(KeyConflictContext.UNIVERSAL.conflicts(KeyConflictContext.IN_GAME));
        Assertions.assertTrue(KeyConflictContext.UNIVERSAL.conflicts(KeyConflictContext.GUI_AND_IN_GAME));
        Assertions.assertTrue(KeyConflictContext.UNIVERSAL.conflicts(KeyConflictContext.SPECTATOR));
        Assertions.assertTrue(KeyConflictContext.UNIVERSAL.conflicts(KeyConflictContext.DEBUG));
        Assertions.assertTrue(KeyConflictContext.UNIVERSAL.conflicts(new TestKeyConflictContext()));
    }

    @Test
    void guiAndInGameContextUsesCurrentGuiState() {
        Assertions.assertEquals(KeyConflictContext.GUI.isActive(), KeyConflictContext.GUI_AND_IN_GAME.requiresExactKeyModifierNone());
    }

    @Test
    void guiAndInGameContextConflictsWithVanillaGuiAndInGameButNotCustomOrSpecialContexts() {
        Assertions.assertTrue(KeyConflictContext.GUI_AND_IN_GAME.conflicts(KeyConflictContext.GUI));
        Assertions.assertTrue(KeyConflictContext.GUI_AND_IN_GAME.conflicts(KeyConflictContext.IN_GAME));
        Assertions.assertTrue(KeyConflictContext.GUI_AND_IN_GAME.conflicts(KeyConflictContext.GUI_AND_IN_GAME));
        Assertions.assertFalse(KeyConflictContext.GUI_AND_IN_GAME.conflicts(KeyConflictContext.SPECTATOR));
        Assertions.assertFalse(KeyConflictContext.GUI_AND_IN_GAME.conflicts(KeyConflictContext.DEBUG));
        Assertions.assertFalse(KeyConflictContext.GUI_AND_IN_GAME.conflicts(new TestKeyConflictContext()));
        Assertions.assertTrue(new TestKeyConflictContext(KeyConflictContext.GUI_AND_IN_GAME).conflicts(KeyConflictContext.GUI_AND_IN_GAME));
    }

    @Test
    void spectatorContextDoesNotRequireExactKeyModifierNone() {
        Assertions.assertFalse(KeyConflictContext.SPECTATOR.requiresExactKeyModifierNone());
    }

    @Test
    void spectatorContextOnlyConflictsWithSpectatorContext() {
        Assertions.assertFalse(KeyConflictContext.SPECTATOR.conflicts(KeyConflictContext.GUI));
        Assertions.assertFalse(KeyConflictContext.SPECTATOR.conflicts(KeyConflictContext.IN_GAME));
        Assertions.assertFalse(KeyConflictContext.SPECTATOR.conflicts(KeyConflictContext.GUI_AND_IN_GAME));
        Assertions.assertTrue(KeyConflictContext.SPECTATOR.conflicts(KeyConflictContext.SPECTATOR));
        Assertions.assertFalse(KeyConflictContext.SPECTATOR.conflicts(KeyConflictContext.DEBUG));
        Assertions.assertFalse(KeyConflictContext.SPECTATOR.conflicts(new TestKeyConflictContext()));
    }

    @Test
    void debugContextDoesNotRequireExactKeyModifierNone() {
        Assertions.assertFalse(KeyConflictContext.DEBUG.requiresExactKeyModifierNone());
    }

    @Test
    void debugContextOnlyConflictsWithDebugContext() {
        Assertions.assertFalse(KeyConflictContext.DEBUG.conflicts(KeyConflictContext.GUI));
        Assertions.assertFalse(KeyConflictContext.DEBUG.conflicts(KeyConflictContext.IN_GAME));
        Assertions.assertFalse(KeyConflictContext.DEBUG.conflicts(KeyConflictContext.GUI_AND_IN_GAME));
        Assertions.assertFalse(KeyConflictContext.DEBUG.conflicts(KeyConflictContext.SPECTATOR));
        Assertions.assertTrue(KeyConflictContext.DEBUG.conflicts(KeyConflictContext.DEBUG));
        Assertions.assertFalse(KeyConflictContext.DEBUG.conflicts(new TestKeyConflictContext()));
    }

    @Test
    void customContextDefaultsToExactKeyModifierNone() {
        Assertions.assertTrue(new TestKeyConflictContext().requiresExactKeyModifierNone());
    }

    private record TestKeyConflictContext(IKeyConflictContext conflictingContext) implements IKeyConflictContext {
        private TestKeyConflictContext() {
            this(null);
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == conflictingContext;
        }
    }
}
