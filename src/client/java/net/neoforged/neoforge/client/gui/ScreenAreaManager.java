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
/// UIs that declare areas are expected to also query the areas of others to avoid overlapping
/// them, excluding their own areas where necessary. How to resolve overlaps beyond that is up
/// to the UIs involved.
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
        return queryAreas(id -> true);
    }

    /// Evaluates the provider registered with the given id and returns the areas it currently
    /// declares, in GUI-scaled absolute screen coordinates.
    ///
    /// @param id the id of the registered area, see [RegisterScreenAreaProviderEvent] and [VanillaScreenAreas]
    /// @return the declared areas, or an empty list if no provider with the given id is registered or applies
    public static List<ScreenRectangle> getOccupiedAreas(Identifier id) {
        return queryAreas(id::equals);
    }

    /// Evaluates all registered providers except the ones registered with the given ids and returns
    /// the areas currently occupied by the other UIs, in GUI-scaled absolute screen coordinates.
    ///
    /// This is meant for UIs that declare their own areas, which must exclude them when placing
    /// themselves to avoid blocking or oscillating around themselves.
    ///
    /// @param excludedIds the ids of the areas to exclude, e.g. the caller's own areas
    /// @return the occupied areas of all other UIs
    public static List<ScreenRectangle> getOccupiedAreasExcluding(Identifier... excludedIds) {
        return queryAreas(notIn(excludedIds));
    }

    /// Checks whether the given area intersects any currently occupied area.
    /// Stops at the first match, so providers after it are not evaluated.
    ///
    /// @param area the area to check, in GUI-scaled absolute screen coordinates
    /// @return true if the area intersects an occupied area
    public static boolean intersectsOccupied(ScreenRectangle area) {
        return anyOccupied(id -> true, occupied -> occupied.intersects(area));
    }

    /// Checks whether the given area intersects any currently occupied area, excluding the areas
    /// registered with the given ids.
    ///
    /// @param area        the area to check, in GUI-scaled absolute screen coordinates
    /// @param excludedIds the ids of the areas to exclude, e.g. the caller's own areas
    /// @return true if the area intersects an occupied area of another UI
    public static boolean intersectsOccupied(ScreenRectangle area, Identifier... excludedIds) {
        return anyOccupied(notIn(excludedIds), occupied -> occupied.intersects(area));
    }

    /// Checks whether the given point lies within any currently occupied area.
    /// Stops at the first match, so providers after it are not evaluated.
    ///
    /// @param x the x coordinate, in GUI-scaled absolute screen coordinates
    /// @param y the y coordinate, in GUI-scaled absolute screen coordinates
    /// @return true if the point lies within an occupied area
    public static boolean containsOccupiedPoint(int x, int y) {
        return anyOccupied(id -> true, occupied -> occupied.containsPoint(x, y));
    }

    /// Checks whether the given point lies within any currently occupied area, excluding the areas
    /// registered with the given ids.
    ///
    /// @param x           the x coordinate, in GUI-scaled absolute screen coordinates
    /// @param y           the y coordinate, in GUI-scaled absolute screen coordinates
    /// @param excludedIds the ids of the areas to exclude, e.g. the caller's own areas
    /// @return true if the point lies within an occupied area of another UI
    public static boolean containsOccupiedPoint(int x, int y, Identifier... excludedIds) {
        return anyOccupied(notIn(excludedIds), occupied -> occupied.containsPoint(x, y));
    }

    private static Predicate<Identifier> notIn(Identifier[] excludedIds) {
        List<Identifier> excluded = List.of(excludedIds);
        return id -> !excluded.contains(id);
    }

    private static ScreenAreaContext createContext() {
        Minecraft minecraft = Minecraft.getInstance();
        return new ScreenAreaContext(minecraft.gui.screen(), minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    private static boolean appliesTo(ScreenAreaRegistration registration, @Nullable Screen screen) {
        return registration.screenClass() == null || (screen != null && registration.screenClass().isInstance(screen));
    }

    private static List<ScreenRectangle> queryAreas(Predicate<Identifier> idFilter) {
        ScreenAreaContext context = createContext();
        List<ScreenRectangle> areas = new ArrayList<>();
        for (Map.Entry<Identifier, ScreenAreaRegistration> entry : REGISTRATIONS.entrySet()) {
            if (idFilter.test(entry.getKey()) && appliesTo(entry.getValue(), context.screen())) {
                collectAreas(entry.getValue().provider(), context, areas);
            }
        }
        return List.copyOf(areas);
    }

    private static boolean anyOccupied(Predicate<Identifier> idFilter, Predicate<ScreenRectangle> test) {
        ScreenAreaContext context = createContext();
        for (Map.Entry<Identifier, ScreenAreaRegistration> entry : REGISTRATIONS.entrySet()) {
            if (idFilter.test(entry.getKey()) && appliesTo(entry.getValue(), context.screen()) && anyMatch(entry.getValue().provider(), context, test)) {
                return true;
            }
        }
        return false;
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
}
