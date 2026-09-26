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
/// [RegisterScreenAreaProviderEvent]. NeoForge itself declares the areas occupied by vanilla
/// UI elements, see [VanillaScreenAreas].
///
/// The areas are reported in GUI-scaled absolute screen coordinates and are re-evaluated for
/// every query. The queries only report these areas, resolving overlaps is up to the UIs involved.
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

    /// Evaluates all registered providers and returns the areas they currently declare.
    ///
    /// @return the occupied areas
    public static List<ScreenArea> getOccupiedAreas() {
        return getOccupiedAreas(_ -> true);
    }

    /// Evaluates the providers whose id matches the given predicate and returns the areas they
    /// currently declare.
    ///
    /// @param idFilter the predicate to test provider ids with
    /// @return the occupied areas
    public static List<ScreenArea> getOccupiedAreas(Predicate<Identifier> idFilter) {
        ScreenAreaContext context = createContext();
        List<ScreenArea> areas = new ArrayList<>();
        for (Map.Entry<Identifier, ScreenAreaRegistration> entry : REGISTRATIONS.entrySet()) {
            if (idFilter.test(entry.getKey()) && appliesTo(entry.getValue(), context)) {
                collectAreas(entry.getKey(), entry.getValue().provider(), context, areas);
            }
        }
        return List.copyOf(areas);
    }

    /// Evaluates all registered providers except the provider registered with the given id and
    /// returns the areas they currently declare.
    ///
    /// @param excludedId the id of the provider to exclude
    /// @return the occupied areas
    public static List<ScreenArea> getOccupiedAreasExcluding(Identifier excludedId) {
        return getOccupiedAreas(id -> !excludedId.equals(id));
    }

    private static ScreenAreaContext createContext() {
        Minecraft minecraft = Minecraft.getInstance();
        return new ScreenAreaContext(minecraft.gui.visibleScreens(), minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    private static boolean appliesTo(ScreenAreaRegistration registration, ScreenAreaContext context) {
        return registration.screenClass() == null || context.screens().stream().anyMatch(registration.screenClass()::isInstance);
    }

    private static void collectAreas(Identifier id, ScreenAreaProvider provider, ScreenAreaContext context, List<ScreenArea> areas) {
        try {
            for (ScreenRectangle area : provider.getAreas(context)) {
                if (area.width() > 0 && area.height() > 0) {
                    areas.add(new ScreenArea(id, area));
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Screen area provider {} threw an exception", provider, exception);
        }
    }
}
