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
     * Gets the object stored by this DeferredHolder, if this holder {@linkplain #isPresent() is present}.
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
     * Gets the object stored by this DeferredHolder, if this holder {@linkplain #isPresent() is present}.
     *
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    @Override
    public T get() {
        return this.value();
    }

    /**
     * @return An optional containing the target object, if {@link #isPresent()}, otherwise {@linkplain Optional#empty() an empty optional}.
     */
    public Optional<T> asOptional() {
        return isPresent() ? Optional.of(value()) : Optional.empty();
    }

    /**
     * @return The registry that this DeferredHolder is pointing at, or {@code null} if it doesn't exist.
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
    protected void bind(boolean throwOnMissingRegistry) {
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

    /**
     * @return True if this DeferredHolder has been bound, and {@link #get()} or {@link #value()} may be called.
     */
    public boolean isPresent() {
        bind(false);
        return this.holder != null;
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
     * @return True if this DH {@link #isPresent()} and the underlying holder {@linkplain Holder#isBound() is bound}.
     */
    @Override
    public boolean isBound() {
        return isPresent() && this.holder.isBound();
    }

    /**
     * @return True if the passed ResourceLocation is the same as the ID of the target object.
     */
    @Override
    public boolean is(ResourceLocation id) {
        return id.equals(this.key.location());
    }

    /**
     * @return True if the passed ResourceKey is the same as this DH's resource key.
     */
    @Override
    public boolean is(ResourceKey<R> key) {
        return key == this.key;
    }

    /**
     * @return True if the filter matches {@linkplain #getKey() this DH's resource key}.
     */
    @Override
    public boolean is(Predicate<ResourceKey<R>> filter) {
        return filter.test(this.key);
    }

    /**
     * @return True if this DH {@link #isPresent()} and the underlying object is a member of the passed tag.
     */
    @Override
    public boolean is(TagKey<R> tag) {
        return isPresent() && this.holder.is(tag);
    }

    /**
     * @return All tags present on the underlying object, if {@link #isPresent()}, otherwise {@linkplain Stream#empty() an empty stream}.
     */
    @Override
    public Stream<TagKey<R>> tags() {
        return isPresent() ? this.holder.tags() : Stream.empty();
    }

    /**
     * If this DH {@linkplain #isPresent() is present}, this method returns an {@link Either#right()} containing the underlying object.
     * Otherwise, this method returns and {@link Either#left()} containing {@linkplain #getKey() this DH's resource key}.
     *
     * @return The unwrapped form of this DeferredHolder.
     */
    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        return isPresent() ? this.holder.unwrap() : Either.left(this.key);
    }

    /**
     * @return An optional containing {@linkplain #getKey() this DH's resource key}.
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
        return isPresent() && this.holder.canSerializeIn(owner);
    }
}
