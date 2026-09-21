/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent;
import net.neoforged.neoforge.client.gui.ScreenAreaContext;
import net.neoforged.neoforge.client.gui.ScreenAreaManager;
import org.junit.jupiter.api.Test;

public class ScreenAreaTests {
    private static final Identifier TEST_AREA = Identifier.fromNamespaceAndPath("test", "area");

    private static Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> newRegistrations() {
        return new LinkedHashMap<>();
    }

    @Test
    void registrationRejectsDuplicateIds() {
        RegisterScreenAreaProviderEvent event = new RegisterScreenAreaProviderEvent(newRegistrations());
        event.registerGlobal(TEST_AREA, context -> List.of());

        assertThrows(IllegalArgumentException.class, () -> event.registerGlobal(TEST_AREA, context -> List.of()));
        assertThrows(IllegalArgumentException.class, () -> event.registerFor(ChatScreen.class, TEST_AREA, context -> List.of()));
    }

    @Test
    void replaceRequiresRegisteredId() {
        RegisterScreenAreaProviderEvent event = new RegisterScreenAreaProviderEvent(newRegistrations());
        assertThrows(IllegalArgumentException.class, () -> event.replace(TEST_AREA, context -> List.of()));
    }

    @Test
    void replaceSwapsProviderAndKeepsScope() {
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = newRegistrations();
        RegisterScreenAreaProviderEvent event = new RegisterScreenAreaProviderEvent(registrations);
        event.registerFor(ChatScreen.class, TEST_AREA, context -> List.of(new ScreenRectangle(0, 0, 10, 10)));

        ScreenRectangle replacementArea = new ScreenRectangle(1, 1, 11, 11);
        event.replace(TEST_AREA, context -> List.of(replacementArea));

        ScreenAreaManager.ScreenAreaRegistration registration = registrations.get(TEST_AREA);
        assertEquals(ChatScreen.class, registration.screenClass());
        assertEquals(List.of(replacementArea), List.copyOf(registration.provider().getAreas(new ScreenAreaContext(null, 100, 100))));
    }

    @Test
    void wrapCanDelegateToTheWrappedProvider() {
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = newRegistrations();
        RegisterScreenAreaProviderEvent event = new RegisterScreenAreaProviderEvent(registrations);
        ScreenRectangle area = new ScreenRectangle(0, 0, 10, 10);
        event.registerGlobal(TEST_AREA, context -> List.of(area));

        event.wrap(TEST_AREA, old -> context -> old.getAreas(context));

        assertSame(area, registrations.get(TEST_AREA).provider().getAreas(new ScreenAreaContext(null, 100, 100)).iterator().next());
    }
}
