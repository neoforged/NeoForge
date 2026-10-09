/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.PatchedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

/// A Deferred Holder is a [Holder] that is constructed with only a ResourceKey.
///
/// It will be populated with the underlying Holder from the registry when available.
///
/// @param <T> The type of object being held by this DeferredHolder.
///
/// @implNote Similar to vanilla's [PatchedRegistry.LazyHolder] we extend [Holder.Reference]
public class DeferredHolder<R, T extends R> extends Holder.Reference<R> implements Supplier<T> {
    /**
     * Creates a new DeferredHolder targeting the value with the specified name in the specified registry.
     *
     * @param <T>         The type of the target value.
     * @param <R>         The registry type.
     * @param registryKey The name of the registry the target value is a member of.
     * @param valueName   The name of the target value.
     */
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, Identifier valueName) {
        return create(ResourceKey.create(registryKey, valueName));
    }

    /**
     * Creates a new DeferredHolder targeting the value with the specified name in the specified registry.
     *
     * @param <T>          The registry type.
     * @param registryName The name of the registry the target value is a member of.
     * @param valueName    The name of the target value.
     */
    public static <R, T extends R> DeferredHolder<R, T> create(Identifier registryName, Identifier valueName) {
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
     * @see #create(ResourceKey, Identifier)
     * @see #create(Identifier, Identifier)
     * @see #create(ResourceKey)
     */
    protected DeferredHolder(ResourceKey<R> key) {
        Objects.requireNonNull(key);
        //Attempt to get the registry, but if it isn't present just pass null and make sure we override any uses of owner in reference
        Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(key.registry());
        super(Holder.Reference.Type.STAND_ALONE, registry, key, null);
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
            throw new NullPointerException("Trying to access unbound value: " + this.key());
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

    /// {@return the registry that this DeferredHolder is pointing at, or `null` if it doesn't exist}
    @Override
    @SuppressWarnings("unchecked")
    public HolderLookup.@Nullable RegistryLookup<R> unwrapLookup() {
        return (Registry<R>) BuiltInRegistries.REGISTRY.getValue(this.key().registry());
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

        HolderLookup.RegistryLookup<R> registry = unwrapLookup();
        if (registry != null) {
            this.holder = registry.get(this.key()).orElse(null);
        } else if (throwOnMissingRegistry) {
            throw new IllegalStateException("Registry not present for " + this + ": " + this.key().registry());
        }
    }

    /**
     * @return The ID of the object pointed to by this DeferredHolder.
     */
    public Identifier getId() {
        return this.key().identifier();
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "DeferredHolder{%s}", this.key());
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

    @Override
    public boolean areComponentsBound() {
        bind(false);
        return this.holder != null && this.holder.areComponentsBound();
    }

    @Override
    public DataComponentMap components() {
        bind(true);
        return this.holder != null ? this.holder.components() : DataComponentMap.EMPTY;
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
     * {@return all tags present on the underlying object}
     *
     * <p>If the underlying object is not {@linkplain #isBound() bound} yet, and empty stream is returned.
     */
    @Override
    public Stream<TagKey<R>> tags() {
        bind(false);
        return this.holder != null ? this.holder.tags() : Stream.empty();
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
