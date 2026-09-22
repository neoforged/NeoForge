/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/// Keeps track of screen areas occupied by UIs, so that other UIs can query and avoid them.
///
/// Occupied areas are declared by [providers][ScreenAreaProvider] and
/// [global providers][GlobalScreenAreaProvider], registered by id via
/// [RegisterScreenAreaProviderEvent]. NeoForge itself declares the areas occupied by vanilla
/// UI elements, see [VanillaScreenAreas].
///
/// The areas are reported in GUI-scaled absolute screen coordinates and are re-evaluated for
/// every query. A query evaluates all applicable providers in a single pass and only reports the
/// areas of the providers the query asks for. The queries only report these areas, resolving
/// overlaps is up to the UIs involved.
///
/// This manager is only usable on the [logical client][LogicalSide#CLIENT].
public class ScreenAreaManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, ScreenAreaRegistration> REGISTRATIONS = new LinkedHashMap<>();
    private static final ThreadLocal<Boolean> EVALUATING = new ThreadLocal<>();
    private static boolean initialized;

    private ScreenAreaManager() {}

    /// A provider registered for an area id, see [RegisterScreenAreaProviderEvent].
    @ApiStatus.Internal
    public sealed interface ScreenAreaRegistration permits ScreenAreaRegistration.Bound, ScreenAreaRegistration.Global {
        /// {@return the ids of the registrations this one is evaluated after}
        List<Identifier> after();

        /// {@return true if this registration is evaluated independently of the visible screens}
        boolean isGlobal();

        /// {@return true if this registration is evaluated for the given screen}
        boolean appliesTo(Screen screen);

        /// Evaluates the provider of this registration.
        ///
        /// @param screen  the screen to evaluate the provider for, `null` for global registrations
        /// @param earlier the areas declared by the registrations evaluated before this one
        /// @param out     accepts the declared areas
        void collect(@Nullable Screen screen, ScreenAreaLookup earlier, Consumer<ScreenArea> out);

        /// {@return a registration for a provider that is evaluated for every screen that is an
        /// instance of the given class}
        ///
        /// @param screenClass the screen class the provider is registered for, including subclasses
        /// @param provider    the provider
        /// @param after       the ids of the registrations to evaluate this one after
        static <S extends Screen> ScreenAreaRegistration bound(Class<S> screenClass, ScreenAreaProvider<? super S> provider, List<Identifier> after) {
            Objects.requireNonNull(screenClass, "screenClass");
            // The provider is only ever evaluated with instances of the screen class it is registered for.
            @SuppressWarnings("unchecked")
            ScreenAreaProvider<Screen> erased = (ScreenAreaProvider<Screen>) Objects.requireNonNull(provider, "provider");
            return new Bound(screenClass, erased, List.copyOf(after));
        }

        /// {@return a registration for a provider that is evaluated regardless of the visible screens}
        ///
        /// @param provider the provider
        /// @param after    the ids of the registrations to evaluate this one after
        static ScreenAreaRegistration global(GlobalScreenAreaProvider provider, List<Identifier> after) {
            return new Global(Objects.requireNonNull(provider, "provider"), List.copyOf(after));
        }

        /// A registration of a [ScreenAreaProvider].
        ///
        /// @param screenClass the screen class the provider is registered for, including subclasses
        /// @param provider    the provider
        /// @param after       the ids of the registrations this one is evaluated after
        record Bound(Class<? extends Screen> screenClass, ScreenAreaProvider<Screen> provider, List<Identifier> after) implements ScreenAreaRegistration {
            @Override
            public boolean isGlobal() {
                return false;
            }

            @Override
            public boolean appliesTo(Screen screen) {
                return this.screenClass.isInstance(screen);
            }

            @Override
            public void collect(@Nullable Screen screen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
                this.provider.collectAreas(this.screenClass.cast(Objects.requireNonNull(screen, "screen")), earlier, out);
            }

            /// {@return a copy of this registration using the given provider}
            public Bound withProvider(ScreenAreaProvider<Screen> provider) {
                return new Bound(this.screenClass, provider, this.after);
            }
        }

        /// A registration of a [GlobalScreenAreaProvider].
        ///
        /// @param provider the provider
        /// @param after    the ids of the registrations this one is evaluated after
        record Global(GlobalScreenAreaProvider provider, List<Identifier> after) implements ScreenAreaRegistration {
            @Override
            public boolean isGlobal() {
                return true;
            }

            @Override
            public boolean appliesTo(Screen screen) {
                return true;
            }

            @Override
            public void collect(@Nullable Screen screen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
                this.provider.collectAreas(earlier, out);
            }

            /// {@return a copy of this registration using the given provider}
            public Global withProvider(GlobalScreenAreaProvider provider) {
                return new Global(provider, this.after);
            }
        }
    }

    @ApiStatus.Internal
    public static void init() {
        if (initialized) {
            throw new IllegalStateException("ScreenAreaManager has already been initialized");
        }
        initialized = true;
        VanillaScreenAreas.register(REGISTRATIONS);
        NeoForge.EVENT_BUS.post(new RegisterScreenAreaProviderEvent(REGISTRATIONS));
    }

    /// Registers a global provider under the given id, for example the provider of the areas
    /// declared by a HUD layer, see [RegisterGuiLayersEvent].
    ///
    /// @param id       the id of the area, must not already be registered
    /// @param provider the provider
    /// @param after    the ids of the registrations to evaluate this one after
    /// @throws IllegalArgumentException if an area with the given id is already registered
    @ApiStatus.Internal
    public static void registerGlobalArea(Identifier id, GlobalScreenAreaProvider provider, Identifier... after) {
        Objects.requireNonNull(id, "id");
        if (REGISTRATIONS.containsKey(id)) {
            throw new IllegalArgumentException("Screen area already registered: " + id);
        }
        REGISTRATIONS.put(id, ScreenAreaRegistration.global(provider, List.of(after)));
    }

    /// Replaces the global provider registered under the given id, for example the provider of the
    /// areas declared by a HUD layer, see [RegisterGuiLayersEvent].
    ///
    /// @param id       the id of the area to replace
    /// @param provider the provider to replace it with
    /// @param after    the ids of the registrations to evaluate this one after
    @ApiStatus.Internal
    public static void replaceGlobalArea(Identifier id, GlobalScreenAreaProvider provider, Identifier... after) {
        Objects.requireNonNull(id, "id");
        REGISTRATIONS.put(id, ScreenAreaRegistration.global(provider, List.of(after)));
    }

    /// Evaluates the providers applicable to the given screen and passes the areas they currently
    /// declare to the given consumer.
    ///
    /// @param screen the screen to evaluate the screen-bound providers for
    /// @param out    accepts the declared areas
    public static void collectScreenAreas(Screen screen, Consumer<ScreenArea> out) {
        Objects.requireNonNull(screen, "screen");
        evaluate(List.of(screen), (id, registration) -> !registration.isGlobal()).forEach(out);
    }

    /// Evaluates the providers applicable to the given screens and passes the areas they currently
    /// declare to the given consumer.
    ///
    /// @param screens the screens to evaluate the screen-bound providers for
    /// @param out     accepts the declared areas
    public static void collectScreenAreas(Iterable<? extends Screen> screens, Consumer<ScreenArea> out) {
        List<Screen> screenList = new ArrayList<>();
        screens.forEach(screenList::add);
        evaluate(screenList, (id, registration) -> !registration.isGlobal()).forEach(out);
    }

    /// Evaluates all registered providers and returns the areas the HUD currently occupies, as
    /// declared by the [global providers][GlobalScreenAreaProvider].
    ///
    /// @param idFilter the predicate to test the provider ids with
    /// @return the occupied areas
    public static List<ScreenArea> getHudAreas(Predicate<Identifier> idFilter) {
        Objects.requireNonNull(idFilter, "idFilter");
        return evaluate(visibleScreens(), (id, registration) -> registration.isGlobal() && idFilter.test(id));
    }

    /// Evaluates all registered providers and returns the areas they currently declare.
    ///
    /// @return the occupied areas
    public static List<ScreenArea> getOccupiedAreas() {
        return getOccupiedAreas(_ -> true);
    }

    /// Evaluates all registered providers whose id matches the given predicate and returns the
    /// areas they currently declare.
    ///
    /// @param idFilter the predicate to test the provider ids with
    /// @return the occupied areas
    public static List<ScreenArea> getOccupiedAreas(Predicate<Identifier> idFilter) {
        Objects.requireNonNull(idFilter, "idFilter");
        return evaluate(visibleScreens(), (id, registration) -> idFilter.test(id));
    }

    /// Evaluates all registered providers except the provider registered with the given id and
    /// returns the areas they currently declare.
    ///
    /// @param excludedId the id of the provider to exclude
    /// @return the occupied areas
    public static List<ScreenArea> getOccupiedAreasExcluding(Identifier excludedId) {
        Objects.requireNonNull(excludedId, "excludedId");
        return getOccupiedAreas(id -> !excludedId.equals(id));
    }

    private static List<Screen> visibleScreens() {
        return Minecraft.getInstance().gui.visibleScreens();
    }

    /// Evaluates all providers applicable to the given screens in a single pass and returns the
    /// areas the given filter accepts.
    private static List<ScreenArea> evaluate(List<Screen> screens, BiPredicate<Identifier, ScreenAreaRegistration> outputFilter) {
        if (Boolean.TRUE.equals(EVALUATING.get())) {
            throw new IllegalStateException("Screen areas must not be queried while they are being evaluated, use the ScreenAreaLookup the provider was given instead");
        }
        EVALUATING.set(Boolean.TRUE);
        try {
            List<Map.Entry<Identifier, ScreenAreaRegistration>> applicable = new ArrayList<>();
            for (Map.Entry<Identifier, ScreenAreaRegistration> entry : REGISTRATIONS.entrySet()) {
                if (entry.getValue().isGlobal() || screens.stream().anyMatch(entry.getValue()::appliesTo)) {
                    applicable.add(entry);
                }
            }

            Lookup lookup = new Lookup();
            List<ScreenArea> areas = new ArrayList<>();
            for (Map.Entry<Identifier, ScreenAreaRegistration> entry : evaluationOrder(applicable)) {
                Identifier id = entry.getKey();
                ScreenAreaRegistration registration = entry.getValue();
                List<ScreenArea> declared = new ArrayList<>();
                try {
                    if (registration.isGlobal()) {
                        registration.collect(null, lookup, declared::add);
                    } else {
                        for (Screen screen : screens) {
                            if (registration.appliesTo(screen)) {
                                registration.collect(screen, lookup, declared::add);
                            }
                        }
                    }
                } catch (Throwable throwable) {
                    LOGGER.error("Screen area provider {} threw an exception", id, throwable);
                }
                for (ScreenArea area : declared) {
                    if (area.bounds().width() > 0 && area.bounds().height() > 0) {
                        lookup.add(area);
                        if (outputFilter.test(id, registration)) {
                            areas.add(area);
                        }
                    }
                }
            }
            return List.copyOf(areas);
        } finally {
            EVALUATING.remove();
        }
    }

    /// Sorts the given registrations so that the providers are evaluated after the ids they depend
    /// on, keeping the registration order for the providers that are not related to each other.
    private static List<Map.Entry<Identifier, ScreenAreaRegistration>> evaluationOrder(List<Map.Entry<Identifier, ScreenAreaRegistration>> registrations) {
        int count = registrations.size();
        Map<Identifier, Integer> indices = new HashMap<>();
        for (int i = 0; i < count; i++) {
            indices.put(registrations.get(i).getKey(), i);
        }

        int[] missingDependencies = new int[count];
        List<List<Integer>> dependents = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            dependents.add(new ArrayList<>());
        }
        for (int i = 0; i < count; i++) {
            for (Identifier after : registrations.get(i).getValue().after()) {
                // Dependencies on ids that are not part of this evaluation are ignored.
                Integer index = indices.get(after);
                if (index != null) {
                    missingDependencies[i]++;
                    dependents.get(index).add(i);
                }
            }
        }

        PriorityQueue<Integer> ready = new PriorityQueue<>();
        for (int i = 0; i < count; i++) {
            if (missingDependencies[i] == 0) {
                ready.add(i);
            }
        }

        List<Integer> sorted = new ArrayList<>(count);
        boolean[] evaluated = new boolean[count];
        while (!ready.isEmpty()) {
            int index = ready.poll();
            sorted.add(index);
            evaluated[index] = true;
            for (int dependent : dependents.get(index)) {
                if (--missingDependencies[dependent] == 0) {
                    ready.add(dependent);
                }
            }
        }

        if (sorted.size() < count) {
            List<Identifier> cyclic = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                if (!evaluated[i]) {
                    cyclic.add(registrations.get(i).getKey());
                }
            }
            LOGGER.error("Screen areas {} cannot be ordered by their dependencies because of a cycle, evaluating them in registration order instead", cyclic);
            // Fall back to the registration order for the registrations that could not be sorted.
            for (int i = 0; i < count; i++) {
                if (!evaluated[i]) {
                    sorted.add(i);
                }
            }
        }

        List<Map.Entry<Identifier, ScreenAreaRegistration>> ordered = new ArrayList<>(count);
        for (int index : sorted) {
            ordered.add(registrations.get(index));
        }
        return ordered;
    }

    /// The [lookup][ScreenAreaLookup] the providers are given during an evaluation.
    private static final class Lookup implements ScreenAreaLookup {
        private final List<ScreenArea> areas = new ArrayList<>();

        void add(ScreenArea area) {
            this.areas.add(area);
        }

        @Override
        public List<ScreenArea> all() {
            return List.copyOf(this.areas);
        }

        @Override
        public List<ScreenArea> excluding(Identifier id) {
            return this.areas.stream().filter(area -> !area.id().equals(id)).toList();
        }

        @Override
        public List<ScreenArea> withId(Identifier id) {
            return this.areas.stream().filter(area -> area.id().equals(id)).toList();
        }
    }
}
