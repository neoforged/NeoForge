/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.resources.ResourceLocation;

/**
 * Helper class to manage registering capabilities.
 * You only need this if you are creating your own type (block, entity, item...) of capabilities!
 *
 * <p>Look at the source code of {@link BlockCapability}, {@link EntityCapability}, ... for an example.
 */
public class CapabilityRegistry<C> {
    private final ConcurrentMap<ResourceLocation, StoredCap<C>> caps = new ConcurrentHashMap<>();
    private final CapabilityConstructor<C> constructor;

    public CapabilityRegistry(CapabilityConstructor<C> constructor) {
        Objects.requireNonNull(constructor);
        this.constructor = constructor;
    }

    /**
     * Creates a new capability with the given name, type class and context class,
     * or returns an existing one if it was already created.
     *
     * @param name         name of the capability
     * @param typeClass    class of the queried object
     * @param contextClass class of the additional context
     * @throws IllegalStateException if a capability with the same name but different type or context class was already created
     */
    public C create(ResourceLocation name, Class<?> typeClass, Class<?> contextClass) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(typeClass);
        Objects.requireNonNull(contextClass);

        StoredCap<C> ret = caps.get(name);
        if (ret == null) {
            ret = caps.computeIfAbsent(name, n -> new StoredCap<>(constructor.create(n, typeClass, contextClass), typeClass, contextClass));
        }

        if (ret.typeClass != typeClass) {
            throw new IllegalStateException("Attempted to register capability " + name + " with existing type class " + ret.typeClass + " != " + typeClass);
        } else if (ret.contextClass != contextClass) {
            throw new IllegalStateException("Attempted to register capability " + name + " with existing context class " + ret.contextClass + " != " + contextClass);
        }

        return ret.cap;
    }

    /**
     * Returns an immutable copy of all the currently known capabilities.
     */
    public List<C> getAll() {
        return caps.values().stream().map(StoredCap::cap).toList();
    }

    @FunctionalInterface
    public interface CapabilityConstructor<C> {
        /**
         * Creates a new capability with the given name, type class and context class.
         *
         * <p>Implementations should use a subclass of {@link BaseCapability} to easily store this metadata.
         */
        C create(ResourceLocation name, Class<?> typeClass, Class<?> contextClass);
    }

    private record StoredCap<C>(C cap, Class<?> typeClass, Class<?> contextClass) {}
}
