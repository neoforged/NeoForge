/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/// Keeps track of screen areas occupied by UIs, so that other UIs can query and avoid them.
///
/// Occupied areas are declared by [providers][ScreenAreaProvider], registered by id via
/// [RegisterScreenAreaProviderEvent]. The areas are re-evaluated on every query, so they
/// always reflect current UI state. NeoForge itself declares the areas occupied by vanilla
/// UI elements, see [VanillaScreenAreas].
///
/// This manager is only usable on the [logical client][LogicalSide#CLIENT].
public class ScreenAreaManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, ScreenAreaRegistration> REGISTRATIONS = new LinkedHashMap<>();
    private static boolean initialized = false;

    private ScreenAreaManager() {}

    /// A registered area provider, see [RegisterScreenAreaProviderEvent].
    ///
    /// @param screenClass the screen class this provider is queried for, or `null` if it is always queried
    /// @param provider    the provider
    @ApiStatus.Internal
    public record ScreenAreaRegistration(@Nullable Class<? extends Screen> screenClass, ScreenAreaProvider provider) {}

    @ApiStatus.Internal
    public static void init() {
        if (initialized) {
            throw new IllegalStateException("ScreenAreaManager has already been initialized");
        }
        initialized = true;
        VanillaScreenAreas.register(REGISTRATIONS);
        NeoForge.EVENT_BUS.post(new RegisterScreenAreaProviderEvent(REGISTRATIONS));
    }

    /// Evaluates all registered providers and returns the areas currently occupied by UIs,
    /// in GUI-scaled absolute screen coordinates.
    ///
    /// @return the occupied areas
    public static List<ScreenRectangle> getOccupiedAreas() {
        ScreenAreaContext context = createContext();
        List<ScreenRectangle> areas = new ArrayList<>();
        for (ScreenAreaRegistration registration : REGISTRATIONS.values()) {
            if (appliesTo(registration, context.screen())) {
                collectAreas(registration.provider(), context, areas);
            }
        }
        return List.copyOf(areas);
    }

    /// Evaluates the provider registered with the given id and returns the areas it currently
    /// declares, in GUI-scaled absolute screen coordinates.
    ///
    /// @param id the id of the registered area, see [RegisterScreenAreaProviderEvent] and [VanillaScreenAreas]
    /// @return the declared areas, or an empty list if no provider with the given id is registered or applies
    public static List<ScreenRectangle> getOccupiedAreas(Identifier id) {
        ScreenAreaRegistration registration = REGISTRATIONS.get(id);
        ScreenAreaContext context = createContext();
        if (registration == null || !appliesTo(registration, context.screen())) {
            return List.of();
        }
        List<ScreenRectangle> areas = new ArrayList<>();
        collectAreas(registration.provider(), context, areas);
        return List.copyOf(areas);
    }

    /// Checks whether the given area intersects any currently occupied area.
    /// Stops at the first match, so providers after it are not evaluated.
    ///
    /// @param area the area to check, in GUI-scaled absolute screen coordinates
    /// @return true if the area intersects an occupied area
    public static boolean intersectsOccupied(ScreenRectangle area) {
        return anyOccupied(occupied -> occupied.intersects(area));
    }

    /// Checks whether the given point lies within any currently occupied area.
    /// Stops at the first match, so providers after it are not evaluated.
    ///
    /// @param x the x coordinate, in GUI-scaled absolute screen coordinates
    /// @param y the y coordinate, in GUI-scaled absolute screen coordinates
    /// @return true if the point lies within an occupied area
    public static boolean containsOccupiedPoint(int x, int y) {
        return anyOccupied(occupied -> occupied.containsPoint(x, y));
    }

    /// Finds the largest sub-area of `bounds` that does not intersect any currently occupied
    /// area. This is the standard way for a UI to negotiate its placement with the UIs that have
    /// declared occupied areas.
    ///
    /// @param bounds the area the UI would like to occupy, in GUI-scaled absolute screen coordinates
    /// @return the largest free sub-area, or `null` if no part of `bounds` is free
    public static @Nullable ScreenRectangle largestFreeAreaWithin(ScreenRectangle bounds) {
        return largestFreeAreaWithin(bounds, getOccupiedAreas());
    }

    /// Finds the largest sub-area of `bounds` that does not intersect any of the given
    /// occupied areas.
    ///
    /// @param bounds        the area the UI would like to occupy, in GUI-scaled absolute screen coordinates
    /// @param occupiedAreas the occupied areas to avoid
    /// @return the largest free sub-area, or `null` if no part of `bounds` is free
    public static @Nullable ScreenRectangle largestFreeAreaWithin(ScreenRectangle bounds, List<ScreenRectangle> occupiedAreas) {
        if (bounds.width() <= 0 || bounds.height() <= 0) {
            return null;
        }
        ScreenRectangle candidate = bounds;
        while (true) {
            ScreenRectangle next = candidate;
            for (ScreenRectangle occupied : occupiedAreas) {
                ScreenRectangle overlap = occupied.intersection(candidate);
                if (overlap != null) {
                    next = largestRemainder(candidate, overlap);
                    if (next == null) {
                        return null;
                    }
                    break;
                }
            }
            if (next == candidate) {
                return candidate;
            }
            candidate = next;
        }
    }

    private static ScreenAreaContext createContext() {
        Minecraft minecraft = Minecraft.getInstance();
        return new ScreenAreaContext(minecraft.gui.screen(), minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    private static boolean appliesTo(ScreenAreaRegistration registration, @Nullable Screen screen) {
        return registration.screenClass() == null || (screen != null && registration.screenClass().isInstance(screen));
    }

    private static void collectAreas(ScreenAreaProvider provider, ScreenAreaContext context, List<ScreenRectangle> areas) {
        try {
            for (ScreenRectangle area : provider.getAreas(context)) {
                if (area != null && area.width() > 0 && area.height() > 0) {
                    areas.add(area);
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Screen area provider {} threw an exception", provider, exception);
        }
    }

    private static boolean anyOccupied(Predicate<ScreenRectangle> test) {
        ScreenAreaContext context = createContext();
        for (ScreenAreaRegistration registration : REGISTRATIONS.values()) {
            if (appliesTo(registration, context.screen()) && anyMatch(registration.provider(), context, test)) {
                return true;
            }
        }
        return false;
    }

    private static boolean anyMatch(ScreenAreaProvider provider, ScreenAreaContext context, Predicate<ScreenRectangle> test) {
        try {
            for (ScreenRectangle area : provider.getAreas(context)) {
                if (area != null && area.width() > 0 && area.height() > 0 && test.test(area)) {
                    return true;
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Screen area provider {} threw an exception", provider, exception);
        }
        return false;
    }

    private static @Nullable ScreenRectangle largestRemainder(ScreenRectangle candidate, ScreenRectangle overlap) {
        ScreenRectangle best = null;
        best = larger(best, new ScreenRectangle(candidate.left(), candidate.top(), overlap.left() - candidate.left(), candidate.height()));
        best = larger(best, new ScreenRectangle(overlap.right(), candidate.top(), candidate.right() - overlap.right(), candidate.height()));
        best = larger(best, new ScreenRectangle(candidate.left(), candidate.top(), candidate.width(), overlap.top() - candidate.top()));
        best = larger(best, new ScreenRectangle(candidate.left(), overlap.bottom(), candidate.width(), candidate.bottom() - overlap.bottom()));
        return best;
    }

    private static @Nullable ScreenRectangle larger(@Nullable ScreenRectangle a, ScreenRectangle b) {
        if (b.width() <= 0 || b.height() <= 0) {
            return a;
        }
        if (a == null) {
            return b;
        }
        return b.width() * b.height() > a.width() * a.height() ? b : a;
    }
}
