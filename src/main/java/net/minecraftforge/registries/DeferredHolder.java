/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * A Deferred Holder is a Holder that is constructed with only a ResourceKey.<br>
 * It will be populated with the underlying Holder from the registry when available.
 * 
 * @param <T> The type of object being held by this DeferredHolder.
 */
// TODO: (Breaking Change) - Add a second generic type for the base registry type
public class DeferredHolder<T> implements Holder<T>
{
    /**
     * The resource key of the target object.
     */
    protected final ResourceKey<T> key;

    /**
     * The currently cached value.
     */
    @Nullable
    protected Holder<T> holder = null;

    /**
     * Creates a new DeferredHolder targeting the value with the specified name in the specified registry.
     * 
     * @param <T>         The type of the target value.
     * @param <R>         The registry type.
     * @param registryKey The name of the registry the target value is a member of.
     * @param valueName   The name of the target value.
     */
    public static <R, T extends R> DeferredHolder<T> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName)
    {
        return create(ResourceKey.create(registryKey, valueName));
    }

    /**
     * Creates a new DeferredHolder targeting the value with the specified name in the specified registry.
     * 
     * @param <T>          The registry type.
     * @param registryName The name of the registry the target value is a member of.
     * @param valueName    The name of the target value.
     */
    public static <T> DeferredHolder<T> create(ResourceLocation registryName, ResourceLocation valueName)
    {
        return create(ResourceKey.createRegistryKey(registryName), valueName);
    }

    /**
     * Creates a new DeferredHolder targeting the specified value.
     * 
     * @param <T> The type of the target value.
     * @param key The resource key of the target value.
     */
    public static <T> DeferredHolder<T> create(ResourceKey<? super T> key)
    {
        return new DeferredHolder<>(key);
    }

    /**
     * Creates a new DeferredHolder with a ResourceKey.<br>
     * Attempts to bind immediately if possible.
     * 
     * @param key The resource key of the target object.
     * @see #create(ResourceKey, ResourceLocation)
     * @see #create(ResourceLocation, ResourceLocation)
     * @see #create(ResourceKey)
     */
    @SuppressWarnings("unchecked")
    protected DeferredHolder(ResourceKey<? super T> key)
    {
        this.key = (ResourceKey<T>) Objects.requireNonNull(key);
        this.bind(false);
    }

    /**
     * Gets the object stored by this DeferredHolder, if this holder {@linkplain #isPresent() is present}.<br>
     * 
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    @Override
    public T value()
    {
        bind(true);
        Objects.requireNonNull(this.holder, () -> "Trying to access unbound value: " + this.key);
        return this.holder.get();
    }

    /**
     * @return If {@link #isPresent()}, an optional containing the target object, otherwise {@linkplain Optional#empty() an empty optional}.
     */
    public Optional<T> getOptional()
    {
        return isPresent() ? Optional.of(value()) : Optional.empty();
    }

    /**
     * The type of the registry is really <? super T> but this saves us some additional ugly casting and doesn't break anything.
     * 
     * @return The registry that this DeferredHolder is pointing at, or null if it doesn't exist.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    protected Registry<T> getRegistry()
    {
        return (Registry<T>) BuiltInRegistries.REGISTRY.get(this.key.registry());
    }

    /**
     * Binds this DeferredHolder to the underlying registry and target object.<br>
     * Has no effect if already bound.
     * 
     * @param throwOnMissingRegistry If true, an exception will be thrown if the registry is absent.
     * @throws IllegalStateException If throwOnMissingRegistry is true and the backing registry is unavailable.
     */
    protected void bind(boolean throwOnMissingRegistry)
    {
        if (this.holder != null) return;

        Registry<T> registry = getRegistry();
        if (registry != null)
        {
            this.holder = registry.getHolder(this.key).orElse(null);
        }
        else if (throwOnMissingRegistry)
        {
            throw new IllegalStateException("Registry not present for " + this + ": " + this.key.registry());
        }
    }

    /**
     * @return The ID of the object pointed to by this DeferredHolder.
     */
    public ResourceLocation getId()
    {
        return this.key.location();
    }

    /**
     * @return The ResourceKey of the object pointed to by this DeferredHolder.
     */
    public ResourceKey<T> getKey()
    {
        return this.key;
    }

    /**
     * @return True if this DeferredHolder has been bound, and {@link #get()} or {@link #value()} may be called.
     */
    public boolean isPresent()
    {
        bind(false);
        return this.holder != null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        return obj instanceof DeferredHolder<?> dh && dh.key == this.key;
    }

    @Override
    public int hashCode()
    {
        return this.key.hashCode();
    }

    @Override
    public String toString()
    {
        return String.format(Locale.ENGLISH, "DeferredHolder{%s}", this.key);
    }

    /**
     * @return True if this DH {@link #isPresent()} and the underlying holder {@linkplain Holder#isBound() is bound}.
     */
    @Override
    public boolean isBound()
    {
        return isPresent() && this.holder.isBound();
    }

    /**
     * @return True if the passed ResourceLocation is the same as the ID of the target object.
     */
    @Override
    public boolean is(ResourceLocation id)
    {
        return id.equals(this.key.location());
    }

    /**
     * @return True if the passed ResourceKey is the same as this DH's resource key.
     */
    @Override
    public boolean is(ResourceKey<T> key)
    {
        return key == this.key;
    }

    /**
     * @return True if the filter matches {@linkplain #getKey() this DH's resource key}.
     */
    @Override
    public boolean is(Predicate<ResourceKey<T>> filter)
    {
        return filter.test(this.key);
    }

    /**
     * @return True if this DH {@link #isPresent()} and the underlying object is a member of the passed tag.
     */
    @Override
    public boolean is(TagKey<T> tag)
    {
        return isPresent() && this.holder.is(tag);
    }

    /**
     * @return All tags present on the underlying object, if {@link #isPresent()}, otherwise {@linkplain Stream#empty() an empty stream}.
     */
    @Override
    public Stream<TagKey<T>> tags()
    {
        return isPresent() ? this.holder.tags() : Stream.empty();
    }

    /**
     * If this DH {@linkplain #isPresent() is present}, this method returns an {@link Either#right()} containing the underlying object.<br>
     * Otherwise, this method returns and {@link Either#left()} containing {@linkplain #getKey() this DH's resource key}.
     * 
     * @return The unwrapped form of this DeferredHolder.
     */
    @Override
    public Either<ResourceKey<T>, T> unwrap()
    {
        return isPresent() ? this.holder.unwrap() : Either.left(this.key);
    }

    /**
     * @return An optional containing {@linkplain #getKey() this DH's resource key}.
     */
    @Override
    public Optional<ResourceKey<T>> unwrapKey()
    {
        return Optional.of(this.key);
    }

    @Override
    public Kind kind()
    {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner)
    {
        return isPresent() && this.holder.canSerializeIn(owner);
    }

    /**
     * As a workaround, this method is provided to allow upcasting the generic type of this DeferredHolder to properly use the Holder methods.
     * @param <R> The type of the registry this DeferredHolder is targeting.
     * @return this
     * @apiNote This method will be removed in a future release, and DeferredHolder will have two generic types.
     */
    @SuppressWarnings({"unchecked"})
    public <R> Holder<R> cast()
    {
        return (Holder<R>) this;
    }
}
