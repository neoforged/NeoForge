/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void freeAreaWithoutOccupiedAreas() {
        ScreenRectangle bounds = new ScreenRectangle(10, 10, 100, 50);
        assertEquals(bounds, ScreenAreaManager.largestFreeAreaWithin(bounds, List.of()));
    }

    @Test
    void freeAreaWithNonIntersectingOccupiedArea() {
        ScreenRectangle bounds = new ScreenRectangle(10, 10, 100, 50);
        ScreenRectangle occupied = new ScreenRectangle(200, 200, 30, 30);
        assertEquals(bounds, ScreenAreaManager.largestFreeAreaWithin(bounds, List.of(occupied)));
    }

    @Test
    void freeAreaAvoidsSingleOccupiedArea() {
        ScreenRectangle bounds = new ScreenRectangle(0, 0, 100, 100);
        // A vertical band cutting the bounds in two halves of 40 and 50 pixels
        ScreenRectangle occupied = new ScreenRectangle(40, 0, 10, 100);
        assertEquals(new ScreenRectangle(50, 0, 50, 100), ScreenAreaManager.largestFreeAreaWithin(bounds, List.of(occupied)));
    }

    @Test
    void freeAreaAvoidsMultipleOccupiedAreas() {
        ScreenRectangle bounds = new ScreenRectangle(0, 0, 100, 100);
        // After avoiding the left band, the remainder (20, 0, 80, 100) still intersects the bottom band
        List<ScreenRectangle> occupied = List.of(
                new ScreenRectangle(0, 0, 20, 100),
                new ScreenRectangle(20, 80, 80, 20));
        assertEquals(new ScreenRectangle(20, 0, 80, 80), ScreenAreaManager.largestFreeAreaWithin(bounds, occupied));
    }

    @Test
    void freeAreaReturnsNullWhenFullyOccupied() {
        ScreenRectangle bounds = new ScreenRectangle(0, 0, 100, 100);
        ScreenRectangle occupied = new ScreenRectangle(-10, -10, 120, 120);
        assertNull(ScreenAreaManager.largestFreeAreaWithin(bounds, List.of(occupied)));
    }

    @Test
    void freeAreaReturnsNullForDegenerateBounds() {
        assertNull(ScreenAreaManager.largestFreeAreaWithin(new ScreenRectangle(0, 0, 0, 100), List.of()));
        assertNull(ScreenAreaManager.largestFreeAreaWithin(new ScreenRectangle(0, 0, 100, 0), List.of()));
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
