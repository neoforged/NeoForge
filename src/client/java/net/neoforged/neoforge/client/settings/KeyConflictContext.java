/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public enum KeyConflictContext implements IKeyConflictContext {
    /**
     * Universal key bindings are always active and will conflict with any other context, including custom
     * contexts supplied by mods.
     * <p>
     * Use this only for bindings that must reserve their key globally. Bindings which are active in both
     * vanilla GUI and in-game contexts, but should not claim every custom context, should use
     * {@link #GUI_AND_IN_GAME} instead.
     * <p>
     * Key bindings are universal by default.
     */
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

    /**
     * GUI key bindings are only active while a {@link Screen} is open.
     * <p>
     * They only conflict with other GUI bindings. This allows the same key to be bound separately for
     * in-game use when no screen is open.
     * <p>
     * GUI bindings require exact {@link KeyModifier#NONE} matching. For example, a bare key in this
     * context will not match while an extra modifier is held.
     */
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

    /**
     * In-game key bindings are only active while no {@link Screen} is open.
     * <p>
     * They only conflict with other in-game bindings. This allows the same key to be bound separately for
     * GUI use while a screen is open.
     * <p>
     * In-game bindings do not require exact {@link KeyModifier#NONE} matching. For example, a
     * bare {@code A} mapping and an {@code A} mapping with {@link KeyModifier#SHIFT} conflict in this context
     * because holding {@link KeyModifier#SHIFT} does not stop the bare key from being active
     * in-game. See {@link #requiresExactKeyModifierNone()}.
     */
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

    /**
     * Key bindings used both while a {@link Screen} is open and while no screen is open.
     * <p>
     * This is active in the same places as {@link #UNIVERSAL}, but this context's own conflict rule
     * only covers the standard {@link #GUI}, {@link #IN_GAME}, and {@link #GUI_AND_IN_GAME}
     * contexts. It does not automatically conflict with every custom context added by mods, although a
     * custom context or {@link #UNIVERSAL} can still choose to conflict with it.
     * <p>
     * This context requires exact {@link KeyModifier#NONE} matching while a GUI is open. For example, a
     * bare key in this context will not match while an extra modifier is held in a GUI, unlike
     * {@link #UNIVERSAL}.
     */
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
    }
}
