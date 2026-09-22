/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.gui.GlobalScreenAreaProvider;
import net.neoforged.neoforge.client.gui.ScreenAreaManager;
import net.neoforged.neoforge.client.gui.ScreenAreaProvider;
import net.neoforged.neoforge.client.gui.VanillaScreenAreas;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/// Allows users to register [screen area providers][ScreenAreaProvider] and
/// [global screen area providers][GlobalScreenAreaProvider], which declare screen areas occupied by
/// a UI so that other UIs can avoid them.
///
/// Every provider is registered with an [Identifier], which allows targeting it individually
/// to [replace][#replace(Identifier, ScreenAreaProvider)] or [wrap][#wrap(Identifier, UnaryOperator)]
/// it, for example to replace the areas that NeoForge declares for vanilla UI elements,
/// see [VanillaScreenAreas].
///
/// A provider may be registered with the ids of other providers it must be evaluated after, which
/// allows it to read their areas through the lookup it is given while it is evaluated. Providers
/// that do not depend on each other are evaluated in registration order. Areas are only evaluated
/// on demand and the ids passed as dependencies may be unknown, in which case they are ignored.
///
/// Providers are queried on demand and must not be registered after this event has been fired.
///
/// This event is not [cancellable][ICancellableEvent].
///
/// This event is fired on the [main NeoForge event bus][NeoForge#EVENT_BUS], only on the [logical client][LogicalSide#CLIENT].
public class RegisterScreenAreaProviderEvent extends Event {
    private final Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations;

    @ApiStatus.Internal
    public RegisterScreenAreaProviderEvent(Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations) {
        this.registrations = registrations;
    }

    /// Registers a provider that is always evaluated, including when no screen is open and only the
    /// HUD is visible.
    ///
    /// @param id       the id of the area, must not already be registered
    /// @param provider the provider
    /// @param after    the ids of the areas to evaluate this provider after
    public void registerGlobal(Identifier id, GlobalScreenAreaProvider provider, Identifier... after) {
        register(id, ScreenAreaManager.ScreenAreaRegistration.global(provider, List.of(after)));
    }

    /// Registers a provider that is only evaluated for screens that are instances of the given
    /// class. Multiple providers may be registered for the same screen class; all of them apply.
    ///
    /// @param screenClass the screen class the provider applies to (including subclasses)
    /// @param id          the id of the area, must not already be registered
    /// @param provider    the provider
    /// @param after       the ids of the areas to evaluate this provider after
    public <S extends Screen> void registerFor(Class<S> screenClass, Identifier id, ScreenAreaProvider<? super S> provider, Identifier... after) {
        register(id, ScreenAreaManager.ScreenAreaRegistration.bound(screenClass, provider, List.of(after)));
    }

    /// Replaces the provider registered with the given id, for example one of the vanilla areas
    /// declared by [VanillaScreenAreas]. The evaluation order of the replaced provider is kept.
    ///
    /// @param id          the id of the provider to replace
    /// @param replacement the provider to replace it with
    /// @throws IllegalArgumentException if no provider with the given id is registered, or if the
    ///         provider registered with the given id is global
    public <S extends Screen> void replace(Identifier id, ScreenAreaProvider<? super S> replacement) {
        Objects.requireNonNull(replacement, "replacement");
        if (requireRegistered(id) instanceof ScreenAreaManager.ScreenAreaRegistration.Bound bound) {
            this.registrations.put(id, bound.withProvider(eraseScreenProvider(replacement)));
        } else {
            throw new IllegalArgumentException("Attempted to replace screen area with id '" + id + "', which is declared by a global provider, use replaceGlobal instead!");
        }
    }

    /// Replaces the global provider registered with the given id. The evaluation order of the
    /// replaced provider is kept.
    ///
    /// @param id          the id of the provider to replace
    /// @param replacement the provider to replace it with
    /// @throws IllegalArgumentException if no provider with the given id is registered, or if the
    ///         provider registered with the given id is not global
    public void replaceGlobal(Identifier id, GlobalScreenAreaProvider replacement) {
        Objects.requireNonNull(replacement, "replacement");
        if (requireRegistered(id) instanceof ScreenAreaManager.ScreenAreaRegistration.Global global) {
            this.registrations.put(id, global.withProvider(replacement));
        } else {
            throw new IllegalArgumentException("Attempted to replace screen area with id '" + id + "', which is declared for a screen, use replace instead!");
        }
    }

    /// Wraps the provider registered with the given id, for example to adjust or conditionally
    /// suppress the areas it declares. The evaluation order of the wrapped provider is kept.
    ///
    /// @param id      the id of the provider to wrap
    /// @param wrapper an unary operator which takes in the old provider and returns the new provider
    /// @throws IllegalArgumentException if no provider with the given id is registered, or if the
    ///         provider registered with the given id is global
    public void wrap(Identifier id, UnaryOperator<ScreenAreaProvider<Screen>> wrapper) {
        Objects.requireNonNull(wrapper, "wrapper");
        if (requireRegistered(id) instanceof ScreenAreaManager.ScreenAreaRegistration.Bound bound) {
            this.registrations.put(id, bound.withProvider(Objects.requireNonNull(wrapper.apply(bound.provider()), "wrapping provider must not be null")));
        } else {
            throw new IllegalArgumentException("Attempted to wrap screen area with id '" + id + "', which is declared by a global provider, use wrapGlobal instead!");
        }
    }

    /// Wraps the global provider registered with the given id, for example to adjust or
    /// conditionally suppress the areas it declares. The evaluation order of the wrapped provider
    /// is kept.
    ///
    /// @param id      the id of the provider to wrap
    /// @param wrapper an unary operator which takes in the old provider and returns the new provider
    /// @throws IllegalArgumentException if no provider with the given id is registered, or if the
    ///         provider registered with the given id is not global
    public void wrapGlobal(Identifier id, UnaryOperator<GlobalScreenAreaProvider> wrapper) {
        Objects.requireNonNull(wrapper, "wrapper");
        if (requireRegistered(id) instanceof ScreenAreaManager.ScreenAreaRegistration.Global global) {
            this.registrations.put(id, global.withProvider(Objects.requireNonNull(wrapper.apply(global.provider()), "wrapping provider must not be null")));
        } else {
            throw new IllegalArgumentException("Attempted to wrap screen area with id '" + id + "', which is declared for a screen, use wrap instead!");
        }
    }

    private ScreenAreaManager.ScreenAreaRegistration requireRegistered(Identifier id) {
        Objects.requireNonNull(id, "id");
        ScreenAreaManager.ScreenAreaRegistration registration = this.registrations.get(id);
        if (registration == null) {
            throw new IllegalArgumentException("Attempted to modify screen area with id '" + id + "', which does not exist!");
        }
        return registration;
    }

    private void register(Identifier id, ScreenAreaManager.ScreenAreaRegistration registration) {
        Objects.requireNonNull(id, "id");
        Preconditions.checkArgument(!this.registrations.containsKey(id), "Screen area already registered: %s", id);
        this.registrations.put(id, registration);
    }

    // Screen-bound providers are only ever evaluated with instances of the screen class they are registered for.
    @SuppressWarnings("unchecked")
    private static ScreenAreaProvider<Screen> eraseScreenProvider(ScreenAreaProvider<?> provider) {
        return (ScreenAreaProvider<Screen>) provider;
    }
}
