/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.gui.ScreenAreaContext;
import net.neoforged.neoforge.client.gui.ScreenAreaManager;
import net.neoforged.neoforge.client.gui.ScreenAreaProvider;
import net.neoforged.neoforge.client.gui.VanillaScreenAreas;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Allows users to register [screen area providers][ScreenAreaProvider], which declare
/// screen areas occupied by a UI so that other UIs can avoid them.
///
/// Every provider is registered with an [Identifier], which allows targeting it individually
/// to [replace][#replace(Identifier, ScreenAreaProvider)] or [wrap][#wrap(Identifier, UnaryOperator)]
/// it, for example to replace the areas that NeoForge declares for vanilla UI elements,
/// see [VanillaScreenAreas].
///
/// Providers are queried on demand, so they should be cheap to evaluate and must not be
/// registered after this event has been fired.
///
/// This event is not [cancellable][ICancellableEvent].
///
/// This event is fired on the mod-specific event bus, only on the [logical client][LogicalSide#CLIENT].
public class RegisterScreenAreaProviderEvent extends Event implements IModBusEvent {
    private final List<ScreenAreaManager.ScreenAreaRegistration> registrations;

    @ApiStatus.Internal
    public RegisterScreenAreaProviderEvent(List<ScreenAreaManager.ScreenAreaRegistration> registrations) {
        this.registrations = registrations;
    }

    /// Registers a provider that is always queried, including when no screen is open and only the
    /// HUD is visible. Use [ScreenAreaContext#isHudContext()] to distinguish the two cases.
    ///
    /// @param id       the id of the area, must not already be registered
    /// @param provider the provider
    public void registerGlobal(Identifier id, ScreenAreaProvider provider) {
        register(id, null, provider);
    }

    /// Registers a provider that is only queried while the open screen is an instance of the given
    /// class. Multiple providers may be registered for the same screen class; all of them apply.
    ///
    /// @param screenClass the screen class the provider applies to (including subclasses)
    /// @param id          the id of the area, must not already be registered
    /// @param provider    the provider
    public void registerFor(Class<? extends Screen> screenClass, Identifier id, ScreenAreaProvider provider) {
        register(id, Objects.requireNonNull(screenClass, "screenClass"), provider);
    }

    /// Replaces the provider registered with the given id, for example one of the vanilla areas
    /// declared by [VanillaScreenAreas].
    ///
    /// @param id          the id of the provider to replace
    /// @param replacement the provider to replace it with
    /// @throws IllegalArgumentException if no provider with the given id is registered
    public void replace(Identifier id, ScreenAreaProvider replacement) {
        wrap(id, old -> replacement);
    }

    /// Wraps the provider registered with the given id, for example to adjust or conditionally
    /// suppress the areas it declares.
    ///
    /// @param id      the id of the provider to wrap
    /// @param wrapper an unary operator which takes in the old provider and returns the new provider
    /// @throws IllegalArgumentException if no provider with the given id is registered
    public void wrap(Identifier id, UnaryOperator<ScreenAreaProvider> wrapper) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(wrapper, "wrapper");

        for (int i = 0; i < this.registrations.size(); i++) {
            ScreenAreaManager.ScreenAreaRegistration registration = this.registrations.get(i);
            if (registration.id().equals(id)) {
                ScreenAreaProvider wrapped = wrapper.apply(registration.provider());
                Objects.requireNonNull(wrapped, "wrapping provider must not be null");
                this.registrations.set(i, new ScreenAreaManager.ScreenAreaRegistration(id, registration.screenClass(), wrapped));
                return;
            }
        }

        throw new IllegalArgumentException("Attempted to wrap screen area with id '" + id + "', which does not exist!");
    }

    private void register(Identifier id, @Nullable Class<? extends Screen> screenClass, ScreenAreaProvider provider) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(provider, "provider");
        for (ScreenAreaManager.ScreenAreaRegistration registration : this.registrations) {
            Preconditions.checkArgument(!registration.id().equals(id), "Screen area already registered: %s", id);
        }
        this.registrations.add(new ScreenAreaManager.ScreenAreaRegistration(id, screenClass, provider));
    }
}
