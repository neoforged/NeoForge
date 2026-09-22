/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent;
import net.neoforged.neoforge.client.gui.ScreenArea;
import net.neoforged.neoforge.client.gui.ScreenAreaManager;
import net.neoforged.neoforge.client.gui.ScreenAreaProvider;
import org.junit.jupiter.api.Test;

public class ScreenAreaTests {
    private static final Identifier A = id("a");
    private static final Identifier B = id("b");
    private static final Identifier C = id("c");
    private static final Identifier D = id("d");
    private static final Identifier E = id("e");
    private static final Identifier GLOBAL = id("global");
    private static final ScreenRectangle BOUNDS = new ScreenRectangle(0, 0, 10, 10);
    // ScreenAreaManager keeps its registrations in a static field and only evaluates them for the
    // screens that are visible in the running client, neither of which can be reproduced in a unit
    // test. The tests swap the registrations of the manager for their own and evaluate those for
    // mock screens, through the same entry points the queries use.
    private static final Field REGISTRATIONS = field("REGISTRATIONS");
    private static final Method EVALUATE = method("evaluate", List.class, BiPredicate.class);

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    private static ScreenArea area(Identifier id) {
        return new ScreenArea(id, BOUNDS);
    }

    private static List<Identifier> ids(List<ScreenArea> areas) {
        return areas.stream().map(ScreenArea::id).toList();
    }

    private static Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations(Consumer<RegisterScreenAreaProviderEvent> registrar) {
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = new LinkedHashMap<>();
        registrar.accept(new RegisterScreenAreaProviderEvent(registrations));
        return registrations;
    }

    /// Evaluates the given registrations for the given screens, as
    /// `ScreenAreaManager#collectScreenAreas(Iterable, Consumer)` does.
    private static List<ScreenArea> collectAreas(Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations, Screen... screens) {
        List<ScreenArea> areas = new ArrayList<>();
        useRegistrations(registrations, () -> ScreenAreaManager.collectScreenAreas(List.of(screens), areas::add));
        return List.copyOf(areas);
    }

    /// Evaluates the given registrations for the given screens, as
    /// `ScreenAreaManager#getOccupiedAreas(Predicate)` does for the given id filter.
    private static List<ScreenArea> queryAreas(Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations, List<Screen> screens, Predicate<Identifier> idFilter) {
        List<ScreenArea> areas = new ArrayList<>();
        useRegistrations(registrations, () -> areas.addAll(evaluate(screens, idFilter)));
        return List.copyOf(areas);
    }

    private static void useRegistrations(Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations, Runnable evaluation) {
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registry = registry();
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registered = new LinkedHashMap<>(registry);
        registry.clear();
        registry.putAll(registrations);
        try {
            evaluation.run();
        } finally {
            registry.clear();
            registry.putAll(registered);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registry() {
        try {
            return (Map<Identifier, ScreenAreaManager.ScreenAreaRegistration>) REGISTRATIONS.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ScreenArea> evaluate(List<Screen> screens, Predicate<Identifier> idFilter) {
        try {
            BiPredicate<Identifier, ScreenAreaManager.ScreenAreaRegistration> filter = (id, registration) -> idFilter.test(id);
            return (List<ScreenArea>) EVALUATE.invoke(null, screens, filter);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Field field(String name) {
        try {
            Field field = ScreenAreaManager.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Method method(String name, Class<?>... parameterTypes) {
        try {
            Method method = ScreenAreaManager.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void registrationRejectsDuplicateIds() {
        RegisterScreenAreaProviderEvent event = new RegisterScreenAreaProviderEvent(new LinkedHashMap<>());
        event.registerGlobal(GLOBAL, (earlier, out) -> {});

        assertThrows(IllegalArgumentException.class, () -> event.registerGlobal(GLOBAL, (earlier, out) -> {}));
        assertThrows(IllegalArgumentException.class, () -> event.registerFor(ScreenA.class, GLOBAL, (screen, earlier, out) -> {}));
    }

    @Test
    void modifyingUnknownIdsIsRejected() {
        RegisterScreenAreaProviderEvent event = new RegisterScreenAreaProviderEvent(new LinkedHashMap<>());

        assertThrows(IllegalArgumentException.class, () -> event.replace(A, (screen, earlier, out) -> {}));
        assertThrows(IllegalArgumentException.class, () -> event.replaceGlobal(GLOBAL, (earlier, out) -> {}));
        assertThrows(IllegalArgumentException.class, () -> event.wrap(A, provider -> provider));
        assertThrows(IllegalArgumentException.class, () -> event.wrapGlobal(GLOBAL, provider -> provider));
    }

    @Test
    void modifyingAnIdOfTheWrongKindIsRejected() {
        RegisterScreenAreaProviderEvent event = new RegisterScreenAreaProviderEvent(new LinkedHashMap<>());
        event.registerGlobal(GLOBAL, (earlier, out) -> {});
        event.registerFor(ScreenA.class, A, (screen, earlier, out) -> {});

        assertThrows(IllegalArgumentException.class, () -> event.replace(GLOBAL, (screen, earlier, out) -> {}));
        assertThrows(IllegalArgumentException.class, () -> event.wrap(GLOBAL, provider -> provider));
        assertThrows(IllegalArgumentException.class, () -> event.replaceGlobal(A, (earlier, out) -> {}));
        assertThrows(IllegalArgumentException.class, () -> event.wrapGlobal(A, provider -> provider));

        // The matching kinds are accepted.
        event.replaceGlobal(GLOBAL, (earlier, out) -> {});
        event.replace(A, (screen, earlier, out) -> {});
        event.wrapGlobal(GLOBAL, provider -> provider);
        event.wrap(A, provider -> provider);
    }

    @Test
    void providersAreEvaluatedInDependencyOrder() {
        Recorder recorder = new Recorder();
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            // Registered in reverse order to show that the declared dependencies decide the order.
            event.registerFor(ScreenA.class, C, recorder.provider(C), B);
            event.registerFor(ScreenA.class, B, recorder.provider(B), A);
            event.registerFor(ScreenA.class, A, recorder.provider(A));
            event.registerFor(ScreenA.class, D, recorder.provider(D));
            event.registerFor(ScreenA.class, E, recorder.provider(E));
        });

        assertEquals(List.of(A, B, C, D, E), ids(collectAreas(registrations, mock(ScreenA.class))));
        assertEquals(List.of(A, B, C, D, E), recorder.order);
        // Every provider is given the areas of all providers that were evaluated before it, whether
        // it declared them as a dependency or not.
        assertEquals(List.of(), recorder.earlier.get(A));
        assertEquals(List.of(area(A)), recorder.earlier.get(B));
        assertEquals(List.of(area(A), area(B)), recorder.earlier.get(C));
        assertEquals(List.of(area(A), area(B), area(C)), recorder.earlier.get(D));
    }

    @Test
    void lookupOnlyExposesTheAreasOfProvidersEvaluatedBefore() {
        List<ScreenArea> all = new ArrayList<>();
        List<ScreenArea> withId = new ArrayList<>();
        List<ScreenArea> excluding = new ArrayList<>();
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerFor(ScreenA.class, A, (screen, earlier, out) -> out.accept(area(A)));
            event.registerFor(ScreenA.class, B, (screen, earlier, out) -> out.accept(area(B)), A);
            event.registerFor(ScreenA.class, C, (screen, earlier, out) -> {
                all.addAll(earlier.all());
                withId.addAll(earlier.withId(A));
                excluding.addAll(earlier.excluding(A));
                out.accept(area(C));
            }, B);
        });

        assertEquals(List.of(A, B, C), ids(collectAreas(registrations, mock(ScreenA.class))));
        assertEquals(List.of(area(A), area(B)), all);
        assertEquals(List.of(area(A)), withId);
        assertEquals(List.of(area(B)), excluding);
    }

    @Test
    void unknownDependenciesAreIgnored() {
        Recorder recorder = new Recorder();
        Identifier unknown = id("unknown");
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerFor(ScreenA.class, B, recorder.provider(B), unknown, A);
            event.registerFor(ScreenA.class, A, recorder.provider(A));
        });

        assertEquals(List.of(A, B), ids(collectAreas(registrations, mock(ScreenA.class))));
        // The unknown dependency is ignored, the known one is still honoured.
        assertEquals(List.of(A, B), recorder.order);
        assertEquals(List.of(area(A)), recorder.earlier.get(B));
    }

    @Test
    void cyclesFallBackToTheRegistrationOrder() {
        Recorder recorder = new Recorder();
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerFor(ScreenA.class, A, recorder.provider(A), B);
            event.registerFor(ScreenA.class, B, recorder.provider(B), A);
        });

        // The providers cannot be ordered, so they are evaluated in registration order instead.
        assertEquals(List.of(A, B), ids(collectAreas(registrations, mock(ScreenA.class))));
        assertEquals(List.of(A, B), recorder.order);
    }

    @Test
    void providersRunForEveryScreenOfTheStackTheyApplyTo() {
        ScreenB background = mock(ScreenB.class);
        ScreenASub top = mock(ScreenASub.class);
        Recorder recorder = new Recorder();
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerFor(ScreenA.class, A, recorder.provider(A));
            event.registerFor(ScreenB.class, B, recorder.provider(B));
            event.registerFor(ScreenC.class, C, recorder.provider(C));
        });

        // The stack is [background, top]: the provider of the background screen runs as well, and
        // each provider is given the screen instance it applies to.
        assertEquals(List.of(A, B), ids(collectAreas(registrations, background, top)));
        assertEquals(List.of(A, B), recorder.order);
        assertSame(top, recorder.screens.get(A));
        assertSame(background, recorder.screens.get(B));
        // A provider registered for a screen class that is not in the stack is not evaluated.
        assertFalse(recorder.screens.containsKey(C));
    }

    @Test
    void replaceKeepsTheEvaluationOrderAndTheScreenClass() {
        List<List<ScreenArea>> replacements = new ArrayList<>();
        ScreenRectangle replacementBounds = new ScreenRectangle(1, 1, 5, 5);
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerFor(ScreenA.class, A, (screen, earlier, out) -> out.accept(area(A)));
            event.registerFor(ScreenA.class, B, (screen, earlier, out) -> out.accept(area(B)), A);
            event.replace(B, (screen, earlier, out) -> {
                replacements.add(earlier.all());
                out.accept(new ScreenArea(B, replacementBounds));
            });
        });

        assertEquals(List.of(area(A), new ScreenArea(B, replacementBounds)), collectAreas(registrations, mock(ScreenASub.class)));
        // The replacement is evaluated in the place of the provider it replaced, so it still sees
        // the area of A.
        assertEquals(List.of(List.of(area(A))), replacements);
        ScreenAreaManager.ScreenAreaRegistration.Bound registration = assertInstanceOf(ScreenAreaManager.ScreenAreaRegistration.Bound.class, registrations.get(B));
        assertEquals(ScreenA.class, registration.screenClass());
    }

    @Test
    void wrapKeepsTheEvaluationOrderAndCanDelegate() {
        Recorder recorder = new Recorder();
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerFor(ScreenA.class, A, recorder.provider(A));
            event.registerFor(ScreenA.class, B, recorder.provider(B), A);
            event.wrap(B, wrapped -> (screen, earlier, out) -> {
                // Only delegate to the wrapped provider if it can still see the area of A, which
                // shows that the wrapped provider keeps its place in the evaluation order.
                if (!earlier.withId(A).isEmpty()) {
                    wrapped.collectAreas(screen, earlier, out);
                }
            });
        });

        assertEquals(List.of(A, B), ids(collectAreas(registrations, mock(ScreenA.class))));
        assertEquals(List.of(area(A)), recorder.earlier.get(B));
    }

    @Test
    void areasWithoutSurfaceAreIgnored() {
        Recorder recorder = new Recorder();
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerFor(ScreenA.class, A, (screen, earlier, out) -> out.accept(new ScreenArea(A, new ScreenRectangle(0, 0, 0, 10))));
            event.registerFor(ScreenA.class, B, recorder.provider(B), A);
        });

        assertEquals(List.of(B), ids(collectAreas(registrations, mock(ScreenA.class))));
        assertEquals(List.of(), recorder.earlier.get(B));
    }

    @Test
    void queryingAreasWhileTheyAreEvaluatedIsRejected() {
        List<Throwable> rejections = new ArrayList<>();
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerFor(ScreenA.class, A, (screen, earlier, out) -> out.accept(area(A)));
            event.registerFor(ScreenA.class, B, (screen, earlier, out) -> {
                // A provider must use the lookup it was given instead of querying the manager, which
                // is checked by the evaluation all query methods share.
                try {
                    ScreenAreaManager.collectScreenAreas(screen, area -> {});
                } catch (Throwable throwable) {
                    rejections.add(throwable);
                }
                out.accept(area(B));
            }, A);
        });

        assertEquals(List.of(A, B), ids(collectAreas(registrations, mock(ScreenA.class))));
        assertEquals(1, rejections.size());
        assertInstanceOf(IllegalStateException.class, rejections.getFirst());
    }

    @Test
    void filteringTheOutputDoesNotAffectTheLookup() {
        Recorder recorder = new Recorder();
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerFor(ScreenA.class, A, recorder.provider(A));
            event.registerFor(ScreenA.class, B, recorder.provider(B), A);
        });
        Screen screen = mock(ScreenA.class);

        // Excluding an area from a query only filters the areas it reports, the providers evaluated
        // after it still see the excluded area.
        assertEquals(List.of(area(B)), queryAreas(registrations, List.of(screen), id -> !A.equals(id)));
        assertEquals(List.of(area(A)), recorder.earlier.get(B));
    }

    @Test
    void screenQueriesOnlyReportTheAreasOfScreenBoundProviders() {
        Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations = registrations(event -> {
            event.registerGlobal(GLOBAL, (earlier, out) -> out.accept(new ScreenArea(GLOBAL, BOUNDS)));
            event.registerFor(ScreenA.class, A, (screen, earlier, out) -> out.accept(area(A)));
        });
        Screen screen = mock(ScreenA.class);

        assertEquals(List.of(area(A)), collectAreas(registrations, screen));
        // The global providers are reported by the queries that ask for them.
        assertEquals(List.of(new ScreenArea(GLOBAL, BOUNDS), area(A)), queryAreas(registrations, List.of(screen), id -> true));
        assertEquals(List.of(new ScreenArea(GLOBAL, BOUNDS)), queryAreas(registrations, List.of(screen), GLOBAL::equals));
    }

    /// Records the providers that were evaluated and what they were given.
    private static final class Recorder {
        private final List<Identifier> order = new ArrayList<>();
        private final Map<Identifier, List<ScreenArea>> earlier = new LinkedHashMap<>();
        private final Map<Identifier, Screen> screens = new LinkedHashMap<>();

        ScreenAreaProvider<Screen> provider(Identifier id) {
            return (screen, lookup, out) -> {
                this.order.add(id);
                this.earlier.put(id, lookup.all());
                this.screens.put(id, screen);
                out.accept(area(id));
            };
        }
    }

    private static class ScreenA extends Screen {
        protected ScreenA() {
            super(Component.empty());
        }
    }

    private static final class ScreenASub extends ScreenA {}

    private static class ScreenB extends Screen {
        protected ScreenB() {
            super(Component.empty());
        }
    }

    private static class ScreenC extends Screen {
        protected ScreenC() {
            super(Component.empty());
        }
    }
}
