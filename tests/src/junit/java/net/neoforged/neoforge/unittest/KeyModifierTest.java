/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class KeyModifierTest {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("neotests", "key_modifier_test"));

    /**
     * Behaves like {@link KeyConflictContext#GUI} while a screen is open, without needing a real {@link net.minecraft.client.gui.Gui}.
     */
    private static final IKeyConflictContext OPEN_SCREEN = new IKeyConflictContext() {
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other || other == KeyConflictContext.GUI;
        }
    };

    private static MockedStatic<Minecraft> mockModifiers(boolean shift, boolean control, boolean alt) {
        Minecraft minecraft = mock(Minecraft.class);
        when(minecraft.hasShiftDown()).thenReturn(shift);
        when(minecraft.hasControlDown()).thenReturn(control);
        when(minecraft.hasAltDown()).thenReturn(alt);
        MockedStatic<Minecraft> mocked = mockStatic(Minecraft.class);
        mocked.when(Minecraft::getInstance).thenReturn(minecraft);
        return mocked;
    }

    private static KeyMapping guiMapping(String name, int keyCode) {
        return new KeyMapping("key.neotests." + name, OPEN_SCREEN, InputConstants.Type.KEYBOARD, keyCode, CATEGORY);
    }

    @Test
    void guiMappingOnModifierKeyIsActiveWhileItsOwnKeyIsHeld() {
        KeyMapping mapping = guiMapping("gui_left_shift", InputConstants.KEY_LSHIFT);
        try (var ignored = mockModifiers(true, false, false)) {
            assertThat(mapping.isConflictContextAndModifierActive()).isTrue();
            assertThat(mapping.isActiveAndMatches(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_LSHIFT))).isTrue();
        }
    }

    @Test
    void guiMappingOnModifierKeyIsInactiveWhileAnotherModifierIsHeld() {
        KeyMapping mapping = guiMapping("gui_left_shift_with_control", InputConstants.KEY_LSHIFT);
        try (var ignored = mockModifiers(true, true, false)) {
            assertThat(mapping.isConflictContextAndModifierActive()).isFalse();
        }
    }

    @Test
    void guiMappingWithoutModifierIsInactiveWhileAModifierIsHeld() {
        KeyMapping mapping = guiMapping("gui_e_with_shift", InputConstants.KEY_E);
        try (var ignored = mockModifiers(true, false, false)) {
            assertThat(mapping.isConflictContextAndModifierActive()).isFalse();
            assertThat(mapping.isActiveAndMatches(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_E))).isFalse();
        }
    }

    @Test
    void guiMappingWithoutModifierIsActiveWhileNoModifierIsHeld() {
        KeyMapping mapping = guiMapping("gui_e", InputConstants.KEY_E);
        try (var ignored = mockModifiers(false, false, false)) {
            assertThat(mapping.isConflictContextAndModifierActive()).isTrue();
            assertThat(mapping.isActiveAndMatches(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_E))).isTrue();
        }
    }
}
