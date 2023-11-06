/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired for each registry when it is ready to have modded objects registered.
 * This event is fired for all registries, including registries in {@link net.minecraft.core.registries.BuiltInRegistries}.
 *
 * <p>This event is fired on the {@linkplain net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * on both {@linkplain net.neoforged.fml.LogicalSide logical sides}.</p>
 *
 * @see #register(ResourceKey, ResourceLocation, Supplier)
 * @see #register(ResourceKey, Consumer)
 */
public class RegisterEvent extends Event implements IModBusEvent
{
    private final ResourceKey<? extends Registry<?>> registryKey;
    private final Registry<?> registry;

    RegisterEvent(ResourceKey<? extends Registry<?>> registryKey, Registry<?> registry)
    {
        this.registryKey = registryKey;
        this.registry = registry;
    }

    /**
     * Registers the value with the given name to the stored registry if the provided registry key matches this event's registry key.
     *
     * @param registryKey the key of the registry to register the value to
     * @param name the name of the object to register as its key
     * @param valueSupplier a supplier of the object value
     * @param <T> the type of the registry
     * @see #register(ResourceKey, Consumer) a register variant making registration of multiple objects less redundant
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation name, Supplier<T> valueSupplier)
    {
        if (this.registryKey.equals(registryKey))
        {
            Registry.register((Registry) this.registry, name, valueSupplier.get());
        }
    }

    /**
     * Calls the provided consumer with a register helper if the provided registry key matches this event's registry key.
     *
     * @param registryKey the key of the registry to register objects to
     * @param <T> the type of the registry
     * @see #register(ResourceKey, ResourceLocation, Supplier) a register variant targeted towards registering one or two objects
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, Consumer<RegisterHelper<T>> consumer)
    {
        if (this.registryKey.equals(registryKey))
        {
            consumer.accept((name, value) -> Registry.register((Registry) this.registry, name, value));
        }
    }

    /**
     * @return The registry key linked to this event
     */
    public ResourceKey<? extends Registry<?>> getRegistryKey()
    {
        return this.registryKey;
    }

    /**
     * @return The registry linked to this event
     */
    public Registry<?> getRegistry()
    {
        return this.registry;
    }

    /**
     * @param key the registry key to compare again {@link #getRegistryKey()}
     * @return The registry typed to the given registry key if it matches {@link #getRegistryKey()},
     * or {@code null} if it does not match.
     */
    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key)
    {
        return key == this.registryKey ? (Registry<T>) this.registry : null;
    }

    @FunctionalInterface
    public interface RegisterHelper<T>
    {
        /**
         * Registers the given value with the given name to the registry.
         * The namespace is inferred based on the active mod container.
         * If you wish to specify a namespace, use {@link #register(ResourceLocation, Object)} instead.
         *
         * @param name the name of the object to register as its key with the namespaced inferred from the active mod container
         * @param value the object value
         */
        default void register(String name, T value)
        {
            register(new ResourceLocation(ModLoadingContext.get().getActiveNamespace(), name), value);
        }

        /**
         * Registers the given value with the given name to the registry.
         *
         * @param key the resource key of the object to register
         * @param value the object value
         */
        default void register(ResourceKey<T> key, T value)
        {
            register(key.location(), value);
        }

        /**
         * Registers the given value with the given name to the registry.
         *
         * @param name the name of the object to register as its key
         * @param value the object value
         */
        void register(ResourceLocation name, T value);
    }
}