/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.settings;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public enum KeyModifier {
    CONTROL {
        private static final InputConstants.Key[] KEYS = new InputConstants.Key[] {
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_CONTROL),
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT_CONTROL)
        };
        private static final InputConstants.Key[] OSX_KEYS = new InputConstants.Key[] {
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SUPER),
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT_SUPER)
        };

        @Override
        public boolean matches(InputConstants.Key key) {
            int keyCode = key.getValue();
            if (Minecraft.ON_OSX) {
                return keyCode == GLFW.GLFW_KEY_LEFT_SUPER || keyCode == GLFW.GLFW_KEY_RIGHT_SUPER;
            } else {
                return keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL;
            }
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext) {
            return Screen.hasControlDown();
        }

        @Override
        public Component getCombinedName(InputConstants.Key key, Supplier<Component> defaultLogic) {
            String localizationFormatKey = Minecraft.ON_OSX ? "neoforge.controlsgui.control.mac" : "neoforge.controlsgui.control";
            return Component.translatable(localizationFormatKey, defaultLogic.get());
        }

        @Override
        public InputConstants.Key[] codes() {
            return Minecraft.ON_OSX ? OSX_KEYS : KEYS;
        }
    },
    SHIFT {
        private static final InputConstants.Key[] KEYS = new InputConstants.Key[] {
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT),
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT_SHIFT)
        };

        @Override
        public boolean matches(InputConstants.Key key) {
            return key.getValue() == GLFW.GLFW_KEY_LEFT_SHIFT || key.getValue() == GLFW.GLFW_KEY_RIGHT_SHIFT;
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext) {
            return Screen.hasShiftDown();
        }

        @Override
        public Component getCombinedName(InputConstants.Key key, Supplier<Component> defaultLogic) {
            return Component.translatable("neoforge.controlsgui.shift", defaultLogic.get());
        }

        @Override
        public InputConstants.Key[] codes() {
            return KEYS;
        }
    },
    ALT {
        private static final InputConstants.Key[] KEYS = new InputConstants.Key[] {
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_ALT),
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT_ALT)
        };

        @Override
        public boolean matches(InputConstants.Key key) {
            return key.getValue() == GLFW.GLFW_KEY_LEFT_ALT || key.getValue() == GLFW.GLFW_KEY_RIGHT_ALT;
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext) {
            return Screen.hasAltDown();
        }

        @Override
        public Component getCombinedName(InputConstants.Key keyCode, Supplier<Component> defaultLogic) {
            return Component.translatable("neoforge.controlsgui.alt", defaultLogic.get());
        }

        @Override
        public InputConstants.Key[] codes() {
            return KEYS;
        }
    },
    NONE {
        private static final InputConstants.Key[] KEYS = new InputConstants.Key[0];

        @Override
        public boolean matches(InputConstants.Key key) {
            return false;
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext) {
            if (conflictContext != null && !conflictContext.conflicts(KeyConflictContext.IN_GAME)) {
                for (KeyModifier keyModifier : MODIFIER_VALUES) {
                    if (keyModifier.isActive(conflictContext)) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public Component getCombinedName(InputConstants.Key key, Supplier<Component> defaultLogic) {
            return defaultLogic.get();
        }

        @Override
        public InputConstants.Key[] codes() {
            return KEYS;
        }
    };

    public static final KeyModifier[] MODIFIER_VALUES = { SHIFT, CONTROL, ALT };

    /** @deprecated Use {@link #getActiveModifiers()} instead. */
    @Deprecated(forRemoval = true, since = "1.21.4")
    public static KeyModifier getActiveModifier() {
        for (KeyModifier keyModifier : MODIFIER_VALUES) {
            if (keyModifier.isActive(null)) {
                return keyModifier;
            }
        }
        return NONE;
    }

    public static List<KeyModifier> getActiveModifiers() {
        List<KeyModifier> modifiers = new ArrayList<>();
        for (KeyModifier keyModifier : MODIFIER_VALUES) {
            if (keyModifier.isActive(null)) {
                modifiers.add(keyModifier);
            }
        }
        return modifiers;
    }

    public static KeyModifier getKeyModifier(InputConstants.Key key) {
        for (KeyModifier keyModifier : MODIFIER_VALUES) {
            if (keyModifier.matches(key)) {
                return keyModifier;
            }
        }
        return NONE;
    }

    public static boolean isKeyCodeModifier(InputConstants.Key key) {
        return getKeyModifier(key) != NONE;
    }

    public static KeyModifier valueFromString(String stringValue) {
        try {
            return valueOf(stringValue);
        } catch (NullPointerException | IllegalArgumentException ignored) {
            return NONE;
        }
    }

    public abstract boolean matches(InputConstants.Key key);

    public abstract boolean isActive(@Nullable IKeyConflictContext conflictContext);

    public abstract Component getCombinedName(InputConstants.Key key, Supplier<Component> defaultLogic);

    // Neo: Make abstract in 1.21.5
    public InputConstants.Key[] codes() {
        return null;
    }
}
