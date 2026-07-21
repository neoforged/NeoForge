/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public enum KeyConflictContext implements IKeyConflictContext {
    /// Universal key bindings are always active and will conflict with any other context, including custom
    /// contexts supplied by mods.
    ///
    /// Use this only for bindings that must reserve their key globally. Bindings which are active in both
    /// vanilla GUI and in-game contexts, but should not claim every custom context, should use
    /// [GUI_AND_IN_GAME][#GUI_AND_IN_GAME] instead.
    ///
    /// Key bindings are universal by default.
    UNIVERSAL {
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return true;
        }

        @Override
        public boolean requiresExactKeyModifierNone() {
            return false;
        }
    },

    /// GUI key bindings are only active while a [Screen] is open.
    ///
    /// They only conflict with other GUI bindings. This allows the same key to be bound separately for
    /// in-game use when no screen is open.
    ///
    /// GUI bindings require exact [NONE][KeyModifier#NONE] matching. For example, a bare key in this
    /// context will not match while an extra modifier is held.
    GUI {
        @SuppressWarnings("ConstantValue")
        @Override
        public boolean isActive() {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft != null && minecraft.gui != null && minecraft.gui.screen() != null;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    },

    /// In-game key bindings are only active while no [Screen] is open.
    ///
    /// They only conflict with other in-game bindings. This allows the same key to be bound separately for
    /// GUI use while a screen is open.
    ///
    /// In-game bindings do not require exact [NONE][KeyModifier#NONE] matching. For example, a
    /// bare `A` mapping and an `A` mapping with [Shift][KeyModifier#SHIFT] conflict in this context
    /// because holding [Shift][KeyModifier#SHIFT] does not stop the bare key from being active
    /// in-game. See [#requiresExactKeyModifierNone()].
    IN_GAME {
        @Override
        public boolean isActive() {
            return !GUI.isActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }

        @Override
        public boolean requiresExactKeyModifierNone() {
            return false;
        }
    },

    /// Key bindings used both while a [Screen] is open and while no screen is open.
    ///
    /// This is active in the same places as [UNIVERSAL][#UNIVERSAL], but this context's own conflict rule
    /// only covers the standard [GUI][#GUI], [IN_GAME][#IN_GAME], and [GUI_AND_IN_GAME][#GUI_AND_IN_GAME]
    /// contexts. It does not automatically conflict with every custom context added by mods, although a
    /// custom context or [UNIVERSAL][#UNIVERSAL] can still choose to conflict with it.
    ///
    /// This context requires exact [NONE][KeyModifier#NONE] matching while a GUI is open. For example, a
    /// bare key in this context will not match while an extra modifier is held in a GUI, unlike
    /// [UNIVERSAL][#UNIVERSAL].
    GUI_AND_IN_GAME {
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == GUI || other == IN_GAME || other == this;
        }

        @Override
        public boolean requiresExactKeyModifierNone() {
            return GUI.isActive();
        }
    },

    /// Spectator key bindings are only active while no [Screen] is open and the local player is in
    /// spectator mode.
    ///
    /// They only conflict with other spectator bindings. This allows bindings such as pick block and the
    /// spectator hotbar action key to share a default key, matching vanilla's mode-specific behavior.
    ///
    /// Spectator bindings do not require exact [NONE][KeyModifier#NONE] matching. For example, a
    /// bare `A` mapping and an `A` mapping with [Shift][KeyModifier#SHIFT] conflict in this context
    /// because holding [Shift][KeyModifier#SHIFT] does not stop the bare key from being active in
    /// spectator mode.
    SPECTATOR {
        @SuppressWarnings("ConstantValue")
        @Override
        public boolean isActive() {
            Minecraft minecraft = Minecraft.getInstance();
            return !GUI.isActive() && minecraft != null && minecraft.player != null && minecraft.player.isSpectator();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }

        @Override
        public boolean requiresExactKeyModifierNone() {
            return false;
        }
    },

    /// Debug key bindings are vanilla debug actions gated by the debug modifier key.
    ///
    /// They only conflict with other debug bindings. Vanilla gives debug shortcuts precedence while
    /// the debug modifier is held, so `F3+A` can reload chunks without making the in-game
    /// `A` binding unassignable.
    ///
    /// The debug modifier key itself remains a separate vanilla key binding rather than a NeoForge
    /// [KeyModifier].
    ///
    /// Debug bindings do not require exact [NONE][KeyModifier#NONE] matching because the debug modifier
    /// key gates the entire debug context instead of acting as each debug binding's key modifier.
    DEBUG {
        @SuppressWarnings("ConstantValue")
        @Override
        public boolean isActive() {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft != null && minecraft.options != null && minecraft.options.keyDebugModifier.isDown();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this;
        }

        @Override
        public boolean requiresExactKeyModifierNone() {
            return false;
        }
    }
}
