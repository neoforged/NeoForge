/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ScreenAreaProvider;
import org.junit.jupiter.api.Test;

public class ScreenAreaTests {
    private static final ScreenRectangle PRIMARY = new ScreenRectangle(1, 2, 300, 200);
    private static final ScreenRectangle EXTRA = new ScreenRectangle(10, 20, 30, 40);
    private static final ScreenRectangle PROVIDED = new ScreenRectangle(50, 60, 70, 80);
    private static final Identifier REQUESTER = Identifier.fromNamespaceAndPath("test", "requester");

    @Test
    void primaryAreaUsesScreenDimensionsByDefault() {
        TestScreen screen = new TestScreen(null);
        screen.width = 300;
        screen.height = 200;

        assertEquals(new ScreenRectangle(0, 0, 300, 200), screen.getPrimaryScreenArea());
    }

    @Test
    void primaryAreaCanBeOverriddenAndQueriedSeparately() {
        TestScreen screen = new TestScreen(PRIMARY);
        screen.extraAreas.add(EXTRA);

        assertEquals(PRIMARY, screen.getPrimaryScreenArea());
        assertEquals(List.of(PRIMARY, EXTRA), collect(screen, true, null));
    }

    @Test
    void queryCanOmitPrimaryArea() {
        TestScreen screen = new TestScreen(PRIMARY);
        screen.extraAreas.add(EXTRA);

        assertEquals(List.of(EXTRA), collect(screen, false, null));
    }

    @Test
    void providersRunAfterTheScreensOwnAreas() {
        TestScreen screen = new TestScreen(PRIMARY);
        screen.extraAreas.add(EXTRA);
        addProvider(screen, (includePrimary, requesterId, out) -> out.accept(PROVIDED));

        assertEquals(List.of(PRIMARY, EXTRA, PROVIDED), collect(screen, true, null));
    }

    @Test
    void queryParametersAreForwardedToEveryProvider() {
        TestScreen screen = new TestScreen(PRIMARY);
        List<Boolean> includePrimary = new ArrayList<>();
        List<Identifier> requesterIds = new ArrayList<>();
        addProvider(screen, (primary, requester, out) -> {
            includePrimary.add(primary);
            requesterIds.add(requester);
        });
        addProvider(screen, (primary, requester, out) -> {
            includePrimary.add(primary);
            requesterIds.add(requester);
        });

        assertEquals(List.of(), collect(screen, false, REQUESTER));
        assertEquals(List.of(false, false), includePrimary);
        assertEquals(2, requesterIds.size());
        assertSame(REQUESTER, requesterIds.getFirst());
        assertSame(REQUESTER, requesterIds.getLast());
    }

    @Test
    void defaultQueryHasNoRequester() {
        TestScreen screen = new TestScreen(PRIMARY);
        List<Identifier> requesterIds = new ArrayList<>();
        addProvider(screen, (includePrimary, requesterId, out) -> requesterIds.add(requesterId));

        screen.getScreenAreas(area -> {});

        assertEquals(1, requesterIds.size());
        assertNull(requesterIds.getFirst());
    }

    @Test
    void providersAreClearedWithWidgets() {
        TestScreen screen = new TestScreen(PRIMARY);
        addProvider(screen, (includePrimary, requesterId, out) -> out.accept(PROVIDED));

        screen.clearWidgetsForTest();

        assertEquals(List.of(PRIMARY), collect(screen, true, null));
    }

    private static void addProvider(TestScreen screen, ScreenAreaProvider provider) {
        new ScreenEvent.Init.Post(screen, List.of(), listener -> {}, listener -> {}).addScreenAreaProvider(provider);
    }

    private static List<ScreenRectangle> collect(TestScreen screen, boolean includePrimary, Identifier requesterId) {
        List<ScreenRectangle> areas = new ArrayList<>();
        screen.getScreenAreas(areas::add, includePrimary, requesterId);
        return areas;
    }

    private static final class TestScreen extends Screen {
        private final ScreenRectangle primaryArea;
        private final List<ScreenRectangle> extraAreas = new ArrayList<>();

        private TestScreen(ScreenRectangle primaryArea) {
            super(org.mockito.Mockito.mock(net.minecraft.client.Minecraft.class), org.mockito.Mockito.mock(net.minecraft.client.gui.Font.class), Component.empty());
            this.primaryArea = primaryArea;
        }

        @Override
        public ScreenRectangle getPrimaryScreenArea() {
            return this.primaryArea != null ? this.primaryArea : super.getPrimaryScreenArea();
        }

        @Override
        protected void collectExtraScreenAreas(Consumer<ScreenRectangle> consumer) {
            this.extraAreas.forEach(consumer);
        }

        private void clearWidgetsForTest() {
            this.clearWidgets();
        }
    }
}
