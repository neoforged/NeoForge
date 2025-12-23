/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.settings;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public enum KeyModifier {
    /**
     * Always matches the Control Key, even on OSX which normally uses the Command Key for hotkeys.
     * Since 1.21.11, Mojang uses Control on OSX for hotkeys that conflict with OSX global system shortcuts.
     * <p>
     * For hotkeys that should use the Command key on OSX (like system hotkeys ⌘C for copy, ⌘P for paste, etc.),
     * use {@link #CONTROL_OR_COMMAND} instead.
     * For examples of places where Command should be used, see uses of
     * `net.minecraft.client.input.InputWithModifiers#hasControlDownWithQuirk()`.
     */
    CONTROL {
        private static final InputConstants.Key[] KEYS = new InputConstants.Key[] {
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_CONTROL),
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT_CONTROL)
        };

        @Override
        public boolean matches(InputConstants.Key key) {
            int keyCode = key.getValue();
            return keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL;
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext) {
            return Minecraft.getInstance().hasControlDown();
        }

        @Override
        public Component getCombinedName(InputConstants.Key key, Supplier<Component> defaultLogic) {
            return Component.translatable("neoforge.controlsgui.control", defaultLogic.get());
        }

        @Override
        public InputConstants.Key[] codes() {
            return KEYS;
        }
    },
    /**
     * On Windows and Linux, this matches the {@link #CONTROL Control} Key.
     * On OSX, this matches the Command Key (⌘).
     * <p>
     * This is the default behavior expected by OSX players for system hotkeys (like ⌘C for copy, ⌘P for paste, etc.),
     * and this follows the {@link InputQuirks#REPLACE_CTRL_KEY_WITH_CMD_KEY} rule.
     * For examples of places where Command should be used, see uses of
     * `net.minecraft.client.input.InputWithModifiers#hasControlDownWithQuirk()`.
     * <p>
     * For hotkeys that should use Control even on OSX, use {@link #CONTROL} instead.
     * <p>
     * Since 1.21.11, Mojang uses {@link #CONTROL Control} on OSX for hotkeys that conflict with OSX global system shortcuts.
     * Use caution when setting default hotkeys with this modifier, because it's possible to end up minimizing
     * the window or triggering some other system behavior instead of what you intended.
     */
    CONTROL_OR_COMMAND {
        private static final InputConstants.Key[] COMMAND_KEYS = new InputConstants.Key[] {
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SUPER),
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT_SUPER),
        };

        @Override
        public boolean matches(InputConstants.Key key) {
            if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
                int keyCode = key.getValue();
                return keyCode == GLFW.GLFW_KEY_LEFT_SUPER || keyCode == GLFW.GLFW_KEY_RIGHT_SUPER;
            }
            return CONTROL.matches(key);
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext) {
            if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
                Minecraft minecraft = Minecraft.getInstance();
                Window window = minecraft.getWindow();
                return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SUPER) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SUPER);
            }
            return CONTROL.isActive(conflictContext);
        }

        @Override
        public Component getCombinedName(InputConstants.Key key, Supplier<Component> defaultLogic) {
            if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
                return Component.translatable("neoforge.controlsgui.control.mac", defaultLogic.get());
            }
            return CONTROL.getCombinedName(key, defaultLogic);
        }

        @Override
        public InputConstants.Key[] codes() {
            if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
                return COMMAND_KEYS;
            }
            return CONTROL.codes();
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
            return Minecraft.getInstance().hasShiftDown();
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
            return Minecraft.getInstance().hasAltDown();
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

    public static final KeyModifier[] MODIFIER_VALUES = InputQuirks.ON_OSX ? new KeyModifier[] { SHIFT, CONTROL_OR_COMMAND, CONTROL, ALT } : new KeyModifier[] { SHIFT, CONTROL, ALT };

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

    public abstract InputConstants.Key[] codes();
}
