/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired when new registries can be constructed and registered.
 * This event is fired to register builtin registries, like the registries in {@link BuiltInRegistries}.
 * Builtin registries are registries which can only load entries registered in code.
 *
 * <p>
 * For registering datapack registries that only load entries through JSON, see {@link DataPackRegistryEvent.NewRegistry}.
 * </p>
 *
 * <p>This event is fired on the {@linkplain net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * on both {@linkplain net.neoforged.fml.LogicalSide logical sides}.</p>
 *
 * @see ModifyRegistriesEvent
 */
public class NewRegistryEvent extends Event implements IModBusEvent {
    private final List<Registry<?>> registries = new ArrayList<>();

    NewRegistryEvent() {}

    /**
     * Creates a registry using the {@code builder} and registers it.
     *
     * @param builder the builder to turn into a {@link Registry}
     * @return the built {@link Registry}
     */
    public <T> Registry<T> create(RegistryBuilder<T> builder) {
        final Registry<T> registry = builder.create();
        register(registry);
        return registry;
    }

    /**
     * Registers an already-created registry.
     * This allows storing registries in static final fields and registering them later.
     *
     * @param registry the registry to register
     */
    public <T> void register(Registry<T> registry) {
        this.registries.add(registry);
    }

    void fill() {
        ((BaseMappedRegistry<?>) BuiltInRegistries.REGISTRY).unfreeze();

        for (final var registry : this.registries) {
            registerToRootRegistry(registry);
        }

        ((WritableRegistry<?>) BuiltInRegistries.REGISTRY).freeze();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void registerToRootRegistry(Registry<?> registry) {
        ResourceLocation registryName = registry.key().location();
        if (BuiltInRegistries.REGISTRY.containsKey(registryName))
            throw new IllegalStateException("Tried to register new registry for " + registryName);

        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(registry.key(), registry, Lifecycle.stable());
    }

}
