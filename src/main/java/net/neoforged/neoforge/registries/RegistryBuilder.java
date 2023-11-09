/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import net.neoforged.neoforge.registries.callback.ClearCallback;
import net.neoforged.neoforge.registries.callback.RegistryCallback;
import org.jetbrains.annotations.Nullable;

public class RegistryBuilder<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final List<RegistryCallback<T>> callbacks = new ArrayList<>();
    @Nullable
    private ResourceLocation defaultKey;
    private int maxId = -1;
    private boolean sync = false;

    public RegistryBuilder(ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public RegistryBuilder<T> defaultKey(ResourceLocation key) {
        this.defaultKey = key;
        return this;
    }

    public RegistryBuilder<T> defaultKey(ResourceKey<T> key) {
        this.defaultKey = key.location();
        return this;
    }

    public RegistryBuilder<T> callback(RegistryCallback<T> callback) {
        this.callbacks.add(callback);
        return this;
    }

    public RegistryBuilder<T> onAdd(AddCallback<T> callback) {
        return this.callback(callback);
    }

    public RegistryBuilder<T> onBake(BakeCallback<T> callback) {
        return this.callback(callback);
    }

    public RegistryBuilder<T> onClear(ClearCallback<T> callback) {
        return this.callback(callback);
    }

    /**
     * Sets the highest numerical id that an entry in this registry
     * is <i>allowed</i> to use.
     * Must be greater than or equal to zero.
     *
     * @param maxId the highest numerical id
     */
    public RegistryBuilder<T> maxId(int maxId) {
        if (maxId < 0)
            throw new IllegalArgumentException("maxId must be greater than or equal to zero");
        this.maxId = maxId;
        return this;
    }

    /**
     * Sets whether this registry should have its numerical IDs synced to clients.
     * Default: {@code false}.
     */
    public RegistryBuilder<T> sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    /**
     * Creates a new registry from this builder.
     * Use {@link NewRegistryEvent#create(RegistryBuilder)} or {@link DeferredRegister#makeRegistry(Consumer)}
     * to not have to call this manually.
     * All created registries must be registered, see {@link NewRegistryEvent#register(Registry)}.
     *
     * @return the created registry
     * @see NewRegistryEvent#register(Registry)
     */
    public Registry<T> create() {
        BaseMappedRegistry<T> registry = this.defaultKey != null
                ? new DefaultedMappedRegistry<>(this.defaultKey.toString(), this.registryKey, Lifecycle.stable(), false)
                : new MappedRegistry<>(this.registryKey, Lifecycle.stable(), false);
        this.callbacks.forEach(registry::addCallback);
        if (this.maxId != -1)
            registry.setMaxId(this.maxId);
        registry.setSync(this.sync);

        return registry;
    }
}
