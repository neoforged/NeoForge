package net.neoforged.neoforge.client.registries;

import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.List;

/// Fired when new client registries can be constructed and registered.
///
/// This event is fired to register builtin client registries, like the registries in [BuiltInClientRegistries].
/// Builtin registries are registries which can only load entries registered in code.
///
/// This event is fired on the mod-specific event bus, only on the [logical client][LogicalSide#CLIENT].
///
/// @see ModifyClientRegistriesEvent
public final class NewClientRegistryEvent extends Event implements IModBusEvent {
    final List<Registry<?>> registries = new ArrayList<>();

    NewClientRegistryEvent() {}

    /// Creates a registry using the `builder` and registers it.
    ///
    /// @param builder the builder to turn into a [Registry]
    /// @return the built [Registry]
    public <T> Registry<T> create(RegistryBuilder<T> builder) {
        final Registry<T> registry = builder.create();
        register(registry);
        return registry;
    }

    /// Registers an already-created registry.
    /// This allows storing registries in static final fields and registering them later.
    ///
    /// @param registry the registry to register
    public <T> void register(Registry<T> registry) {
        if (registry.doesSync()) {
            throw new IllegalArgumentException("Client registries cannot be marked as synced");
        }
        this.registries.add(registry);
    }
}
