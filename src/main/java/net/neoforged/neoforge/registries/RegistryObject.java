/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Superclass of {@link DeferredHolder} which provides access to the underlying object, its bound status, and its key
 * without providing the functionality of {@link Holder}.
 * <p>
 * Users may type-erase their {@link DeferredHolder} fields to this class if they do not need the functionality of {@link Holder}.
 * 
 * @param <T> The type of the target object
 * 
 * @see {@link DeferredHolder} for the implementation of this class
 */
public interface RegistryObject<T> extends Supplier<T> {
    /**
     * {@return true if the underlying object is available}
     */
    boolean isBound();

    /**
     * Gets the target object if this registry object {@linkplain #isBound() is bound}.
     *
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    @Override
    T get();

    /**
     * If this registry object {@linkplain #isBound() is bound}, returns an optional containing the target, or an empty optional if unbound.
     */
    Optional<T> asOptional();

    /**
     * {@return the resource location of the target object}
     */
    ResourceLocation getId();

    /**
     * {@return the resource key of the target object}
     */
    ResourceKey<? super T> getKey();

    /**
     * {@return true if the passed location is the same as the target resource location}
     */
    boolean is(ResourceLocation id);

    /**
     * {@return true if the passed key is the same as the target resource key}
     */
    boolean is(ResourceKey<? super T> key);

    /**
     * Checks if this registry object points to an object from the given registry.
     * 
     * @param <R>         The registry type
     * @param registryKey The registry key
     * @return {@code true} if the target object is (or would be) a member of the given registry.
     */
    default <R> boolean isFor(ResourceKey<? extends Registry<R>> registryKey) {
        return this.getKey().isFor(registryKey);
    }
}
