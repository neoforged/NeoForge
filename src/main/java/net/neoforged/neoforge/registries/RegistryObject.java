/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RegistryObject<T> implements Supplier<T> {
    private final ResourceLocation name;
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final ResourceKey<T> key;
    @Nullable
    private T value;
    @Nullable
    private Holder<T> holder;

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from the provided registry once it is ready.
     *
     * @param name     the name of the object to look up in the registry
     * @param registry the registry
     * @return a {@link RegistryObject} that stores the value of an object from the provided registry once it is ready
     * @see #create(ResourceLocation, ResourceKey)
     * @see #create(ResourceLocation, ResourceLocation)
     */
    public static <T, U extends T> RegistryObject<U> create(final ResourceLocation name, Registry<T> registry) {
        return new RegistryObject<>(name, registry.key().location());
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from a registry once it is ready based on a lookup of the provided registry key.
     * <p>
     * If a registry with the given key cannot be found, an exception will be thrown when trying to fill this RegistryObject.
     *
     * @param name        the name of the object to look up in a registry
     * @param registryKey the key of the registry
     * @return a {@link RegistryObject} that stores the value of an object from a registry once it is ready
     * @see #create(ResourceLocation, Registry)
     * @see #create(ResourceLocation, ResourceLocation)
     */
    public static <T, U extends T> RegistryObject<U> create(final ResourceLocation name, final ResourceKey<? extends Registry<T>> registryKey) {
        return new RegistryObject<>(name, registryKey.location());
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from a registry once it is ready based on a lookup of the provided registry name.
     * <p>
     * If a registry with the given name cannot be found, an exception will be thrown when trying to fill this RegistryObject.
     *
     * @param name         the name of the object to look up in a registry
     * @param registryName the name of the registry
     * @return a {@link RegistryObject} that stores the value of an object from a registry once it is ready
     * @see #create(ResourceLocation, Registry)
     * @see #create(ResourceLocation, ResourceKey)
     */
    public static <T, U extends T> RegistryObject<U> create(final ResourceLocation name, final ResourceLocation registryName) {
        return new RegistryObject<>(name, registryName);
    }

    private RegistryObject(final ResourceLocation name, final ResourceLocation registryName) {
        this.name = name;
        this.registryKey = ResourceKey.createRegistryKey(registryName);
        this.key = ResourceKey.create(registryKey, name);
        ObjectHolderRegistry.addHandler(new Consumer<>() {
            private boolean registryExists = false;
            private boolean invalidRegistry = false;

            @Override
            public void accept(Predicate<ResourceLocation> pred) {
                if (invalidRegistry)
                    return;
                if (!registryExists) {
                    if (!registryExists(registryName)) {
                        invalidRegistry = true;
                        throw new IllegalStateException("Unable to find registry with key " + registryName);
                    }
                    registryExists = true;
                }
                if (pred.test(registryName))
                    RegistryObject.this.updateReference(registryName);
            }
        });
        this.updateReference(registryName);
    }

    /**
     * Retrieves the wrapped object in the registry.
     * This method's return value will automatically be updated when the backing registry is updated.
     *
     * @throws NullPointerException If the value is null. Use {@link #isPresent()} to check if the value exists first.
     * @see #isPresent()
     * @see #orElse(Object)
     * @see #orElseGet(Supplier)
     * @see #orElseThrow(Supplier)
     * @see #asOptional()
     */
    @NotNull
    @Override
    public T get() {
        T ret = this.value;
        Objects.requireNonNull(ret, () -> "Registry Object not present: " + this.name);
        return ret;
    }

    @SuppressWarnings("unchecked")
    void updateReference(Registry<? extends T> registry) {
        if (registry.containsKey(this.name)) {
            this.value = registry.get(this.name);
            this.holder = ((Registry<T>) registry).getHolder(this.key).orElse(null);
        } else {
            this.value = null;
            this.holder = null;
        }
    }

    @SuppressWarnings("unchecked")
    void updateReference(ResourceLocation registryName) {
        Registry<? extends T> registry = (Registry<? extends T>) BuiltInRegistries.REGISTRY.get(registryName);
        if (registry != null)
            updateReference(registry);
    }

    void updateReference(RegisterEvent event) {
        Registry<? extends T> registry = event.getRegistry(this.registryKey);
        if (registry != null)
            updateReference(registry);
        else
            this.value = null;
    }

    private static boolean registryExists(ResourceLocation registryName) {
        return BuiltInRegistries.REGISTRY.containsKey(registryName);
    }

    public ResourceLocation getId() {
        return this.name;
    }

    /**
     * Returns the resource key that points to the registry and name of this registry object.
     * Nullable only if this RegistryObject is empty and has no name.
     *
     * @return the resource key that points to the registry and name of this registry object
     */
    @Nullable
    public ResourceKey<T> getKey() {
        return this.key;
    }

    public Stream<T> stream() {
        return isPresent() ? Stream.of(get()) : Stream.of();
    }

    /**
     * Return {@code true} if there is a mod object present, otherwise {@code false}.
     *
     * @return {@code true} if there is a mod object present, otherwise {@code false}
     */
    public boolean isPresent() {
        return this.value != null;
    }

    /**
     * Return the mod object if present, otherwise return {@code other}.
     *
     * @param other the mod object to be returned if there is no mod object present, may
     *              be null
     * @return the mod object, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return isPresent() ? get() : other;
    }

    /**
     * Return the mod object if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code Supplier} whose result is returned if no mod object
     *              is present
     * @return the mod object if present otherwise the result of {@code other.get()}
     * @throws NullPointerException if mod object is not present and {@code other} is
     *                              null
     */
    public T orElseGet(Supplier<? extends T> other) {
        return isPresent() ? get() : other.get();
    }

    /**
     * Return the contained mod object, if present, otherwise throw an exception
     * to be created by the provided supplier.
     *
     * @apiNote A method reference to the exception constructor with an empty
     *          argument list can be used as the supplier. For example,
     *          {@code IllegalStateException::new}
     *
     * @param <X>               Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to
     *                          be thrown
     * @return the present mod object
     * @throws X                    if there is no mod object present
     * @throws NullPointerException if no mod object is present and
     *                              {@code exceptionSupplier} is null
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return get();
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * {@return an optional of the contained mod object, if present}
     */
    public Optional<T> asOptional() {
        return isPresent() ? Optional.of(get()) : Optional.empty();
    }

    /**
     * Returns an optional {@link Holder} instance pointing to this RegistryObject's name and value.
     * <p>
     * This should <b>only</b> be used in cases where vanilla code requires passing in a Holder.
     * Mod-written code should rely on RegistryObjects or Suppliers instead.
     * <p>
     * The returned optional will be empty if the registry does not exist or if {@link #isPresent() returns false}.
     *
     * @return an optional {@link Holder} instance pointing to this RegistryObject's name and value
     */
    @NotNull
    public Optional<Holder<T>> getHolder() {
        return Optional.ofNullable(this.holder);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof RegistryObject) {
            return Objects.equals(((RegistryObject<?>) obj).name, name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
