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
        Assertions.assertTrue(KeyConflictContext.UNIVERSAL.conflicts(new TestKeyConflictContext()));
    }

    @Test
    void guiAndInGameContextUsesCurrentGuiState() {
        Assertions.assertEquals(KeyConflictContext.GUI.isActive(), KeyConflictContext.GUI_AND_IN_GAME.requiresExactKeyModifierNone());
    }

    @Test
    void guiAndInGameContextConflictsWithVanillaGuiAndInGameButNotCustomContexts() {
        Assertions.assertTrue(KeyConflictContext.GUI_AND_IN_GAME.conflicts(KeyConflictContext.GUI));
        Assertions.assertTrue(KeyConflictContext.GUI_AND_IN_GAME.conflicts(KeyConflictContext.IN_GAME));
        Assertions.assertTrue(KeyConflictContext.GUI_AND_IN_GAME.conflicts(KeyConflictContext.GUI_AND_IN_GAME));
        Assertions.assertFalse(KeyConflictContext.GUI_AND_IN_GAME.conflicts(new TestKeyConflictContext()));
        Assertions.assertTrue(new TestKeyConflictContext(KeyConflictContext.GUI_AND_IN_GAME).conflicts(KeyConflictContext.GUI_AND_IN_GAME));
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
