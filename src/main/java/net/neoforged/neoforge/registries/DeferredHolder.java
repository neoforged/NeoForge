/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.datafixers.util.Either;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

/**
 * A Deferred Holder is a {@link Holder} that is constructed with only a ResourceKey.
 *
 * <p>It will be populated with the underlying Holder from the registry when available.
 *
 * @param <T> The type of object being held by this DeferredHolder.
 */
public class DeferredHolder<R, T extends R> implements Holder<R>, Supplier<T> {
    /**
     * Creates a new DeferredHolder targeting the value with the specified name in the specified registry.
     *
     * @param <T>         The type of the target value.
     * @param <R>         The registry type.
     * @param registryKey The name of the registry the target value is a member of.
     * @param valueName   The name of the target value.
     */
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName) {
        return create(ResourceKey.create(registryKey, valueName));
    }

    /**
     * Creates a new DeferredHolder targeting the value with the specified name in the specified registry.
     *
     * @param <T>          The registry type.
     * @param registryName The name of the registry the target value is a member of.
     * @param valueName    The name of the target value.
     */
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceLocation registryName, ResourceLocation valueName) {
        return create(ResourceKey.createRegistryKey(registryName), valueName);
    }

    /**
     * Creates a new DeferredHolder targeting the specified value.
     *
     * @param <T> The type of the target value.
     * @param key The resource key of the target value.
     */
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    /**
     * The resource key of the target object.
     */
    protected final ResourceKey<R> key;

    /**
     * The currently cached value.
     */
    @Nullable
    private Holder<R> holder = null;

    /**
     * Creates a new DeferredHolder with a ResourceKey.
     *
     * <p>Attempts to bind immediately if possible.
     *
     * @param key The resource key of the target object.
     * @see #create(ResourceKey, ResourceLocation)
     * @see #create(ResourceLocation, ResourceLocation)
     * @see #create(ResourceKey)
     */
    protected DeferredHolder(ResourceKey<R> key) {
        this.key = Objects.requireNonNull(key);
        this.bind(false);
    }

    /**
     * Gets the object stored by this DeferredHolder, if this holder {@linkplain #isBound() is bound}.
     *
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    @SuppressWarnings("unchecked")
    @Override
    public T value() {
        bind(true);
        if (this.holder == null) {
            throw new NullPointerException("Trying to access unbound value: " + this.key);
        }

        return (T) this.holder.value();
    }

    /**
     * Gets the object stored by this DeferredHolder, if this holder {@linkplain #isBound() is bound}.
     *
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    @Override
    public T get() {
        return this.value();
    }

    /**
     * Returns an optional containing the target object, if {@link #isBound() bound}; otherwise {@linkplain Optional#empty() an empty optional}.
     *
     * @return an optional containing the target object, if {@link #isBound() bound}; otherwise {@linkplain Optional#empty() an empty optional}
     */
    public Optional<T> asOptional() {
        return isBound() ? Optional.of(value()) : Optional.empty();
    }

    /**
     * Returns the registry that this DeferredHolder is pointing at, or {@code null} if it doesn't exist.
     *
     * @return the registry that this DeferredHolder is pointing at, or {@code null} if it doesn't exist
     */
    @Nullable
    @SuppressWarnings("unchecked")
    protected Registry<R> getRegistry() {
        return (Registry<R>) BuiltInRegistries.REGISTRY.get(this.key.registry());
    }

    /**
     * Binds this DeferredHolder to the underlying registry and target object.
     *
     * <p>Has no effect if already bound.
     *
     * @param throwOnMissingRegistry If true, an exception will be thrown if the registry is absent.
     * @throws IllegalStateException If throwOnMissingRegistry is true and the backing registry is unavailable.
     */
    protected final void bind(boolean throwOnMissingRegistry) {
        if (this.holder != null) return;

        Registry<R> registry = getRegistry();
        if (registry != null) {
            this.holder = registry.getHolder(this.key).orElse(null);
        } else if (throwOnMissingRegistry) {
            throw new IllegalStateException("Registry not present for " + this + ": " + this.key.registry());
        }
    }

    /**
     * @return The ID of the object pointed to by this DeferredHolder.
     */
    public ResourceLocation getId() {
        return this.key.location();
    }

    /**
     * @return The ResourceKey of the object pointed to by this DeferredHolder.
     */
    public ResourceKey<R> getKey() {
        return this.key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof DeferredHolder<?, ?> dh && dh.key == this.key;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "DeferredHolder{%s}", this.key);
    }

    /**
     * {@return true if the underlying object is available}
     *
     * <p>If {@code true}, the underlying object was added to the registry,
     * and {@link #value()} or {@link #get()} can be called.
     */
    @Override
    public boolean isBound() {
        bind(false);
        return this.holder != null && this.holder.isBound();
    }

    /**
     * {@return true if the passed ResourceLocation is the same as the ID of the target object}
     */
    @Override
    public boolean is(ResourceLocation id) {
        return id.equals(this.key.location());
    }

    /**
     * {@return true if the passed ResourceKey is the same as this holder's resource key}
     */
    @Override
    public boolean is(ResourceKey<R> key) {
        return key == this.key;
    }

    /**
     * Evaluates the passed predicate against this holder's resource key.
     *
     * @return {@code true} if the filter matches {@linkplain #getKey() this DH's resource key}
     */
    @Override
    public boolean is(Predicate<ResourceKey<R>> filter) {
        return filter.test(this.key);
    }

    /**
     * {@return true if this holder is a member of the passed tag}
     */
    @Override
    public boolean is(TagKey<R> tag) {
        bind(false);
        return this.holder != null && this.holder.is(tag);
    }

    /**
     * {@return {@code true} if the {@code holder} is the same as this holder}
     */
    @Override
    @Deprecated
    public boolean is(Holder<R> holder) {
        bind(false);
        return this.holder != null && this.holder.is(holder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <Z> @Nullable Z getData(DataMapType<R, Z> type) {
        bind(false);
        return holder == null ? null : holder.getData(type);
    }

    /**
     * {@return all tags present on the underlying object}
     *
     * <p>If the underlying object is not {@linkplain #isBound() bound} yet, and empty stream is returned.
     */
    @Override
    public Stream<TagKey<R>> tags() {
        bind(false);
        return this.holder != null ? this.holder.tags() : Stream.empty();
    }

    /**
     * Returns an {@link Either#left()} containing {@linkplain #getKey() the resource key of this holder}.
     *
     * @apiNote This method is implemented for {@link Holder} compatibility, but {@link #getKey()} should be preferred.
     */
    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        // Holder.Reference always returns the key, do the same here.
        return Either.left(this.key);
    }

    /**
     * Returns the resource key of this holder.
     *
     * @return a present optional containing {@linkplain #getKey() the resource key of this holder}
     * @apiNote This method is implemented for {@link Holder} compatibility, but {@link #getKey()} should be preferred.
     */
    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<R> owner) {
        bind(false);
        return this.holder != null && this.holder.canSerializeIn(owner);
    }

    @Override
    public Holder<R> getDelegate() {
        bind(false);
        return this.holder != null ? this.holder.getDelegate() : this;
    }
}
