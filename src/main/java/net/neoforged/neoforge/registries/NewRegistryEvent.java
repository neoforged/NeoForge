/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Fired when new registries can be constructed and registered.
 * This event is fired to register builtin registries, like the registries in {@link BuiltInRegistries}.
 * Builtin registries are registries which can only load entries registered in code.
 *
 * <p>
 * For registering datapack registries that only load entries through JSON, see {@link DataPackRegistryEvent.NewRegistry}.
 * </p>
 *
 * <p>This event is not {@linkplain Cancelable cancellable}, and does not {@linkplain HasResult have a result}.</p>
 * <p>This event is fired on the {@linkplain FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * on both {@linkplain LogicalSide logical sides}.</p>
 *
 * @see ModifyRegistryEvent
 */
public class NewRegistryEvent extends Event implements IModBusEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<Object> registries = new ArrayList<>();

    public NewRegistryEvent() {}

    /**
     * Adds a registry builder to be created.
     *
     * @param builder The builder to turn into a {@link Registry}
     * @return A supplier of the {@link Registry} created by the builder. Resolving too early will return null.
     * @see #create(RegistryBuilder, Consumer)
     */
    public <T> Supplier<Registry<T>> create(RegistryBuilder<T> builder) {
        return create(builder, null);
    }

    /**
     * Adds a registry builder to be created.
     *
     * @param builder The builder to turn into a {@link Registry}
     * @param onFill Called when the returned supplier is filled with the registry
     * @return a supplier of the {@link Registry} created by the builder. Resolving too early will return null.
     * @see #create(RegistryBuilder)
     */
    public <T> Supplier<Registry<T>> create(RegistryBuilder<T> builder, @Nullable Consumer<Registry<T>> onFill) {
        RegistryHolder<T> registryHolder = new RegistryHolder<>();

        this.registries.add(new RegistryData<>(builder, registryHolder, onFill));

        return registryHolder;
    }

    /**
     * Registers an already-created registry.
     * This allows storing registries in static final fields and registering them later.
     *
     * @param registry The registry to register
     */
    public <T> void register(Registry<T> registry) {
        this.registries.add(registry);
    }

    void fill() {
        RuntimeException aggregate = new RuntimeException();

        if (BuiltInRegistries.REGISTRY instanceof BaseNeoRegistry<?> rootRegistry)
            rootRegistry.unfreeze();

        for (Object obj : this.registries) {
            try {
                if (obj instanceof RegistryData<?> data) {
                    buildRegistry(data);
                } else if (obj instanceof Registry<?> registry) {
                    registerToRootRegistry(registry);
                }
            } catch (Throwable t) {
                aggregate.addSuppressed(t);
                return;
            }
        }

        if (BuiltInRegistries.REGISTRY instanceof WritableRegistry<?> rootRegistry)
            rootRegistry.freeze();

        if (aggregate.getSuppressed().length > 0)
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to create some forge registries, see suppressed exceptions for details", aggregate);
    }

    private <T> void buildRegistry(RegistryData<T> data) {
        RegistryBuilder<T> builder = data.builder;
        Registry<T> registry = builder.create();

        registerToRootRegistry(registry);

        data.registryHolder.registry = registry;
        if (data.onFill != null)
            data.onFill.accept(registry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerToRootRegistry(Registry<?> registry) {
        ResourceLocation registryName = registry.key().location();
        if (BuiltInRegistries.REGISTRY.containsKey(registryName))
            throw new IllegalStateException("Tried to register new registry for " + registryName);

        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(registry.key(), registry, Lifecycle.stable());
    }

    private record RegistryData<T>(
            RegistryBuilder<T> builder,
            RegistryHolder<T> registryHolder,
            Consumer<Registry<T>> onFill
    ) {}

    private static class RegistryHolder<T> implements Supplier<Registry<T>> {
        Registry<T> registry = null;

        @Override
        public Registry<T> get() {
            return this.registry;
        }
    }
}
