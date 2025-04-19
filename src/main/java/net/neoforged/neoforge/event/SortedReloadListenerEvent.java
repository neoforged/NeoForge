/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class for {@link AddServerReloadListenersEvent} and {@code AddClientReloadListenersEvent}.
 * <p>
 * This class holds the sorting logic that allows for the creation of dependency ordering.
 */
public abstract class SortedReloadListenerEvent extends Event {
    private final Map<ResourceLocation, PreparableReloadListener> registry = new LinkedHashMap<>();
    private final Map<PreparableReloadListener, ResourceLocation> keys = new IdentityHashMap<>();
    private final MutableGraph<PreparableReloadListener> graph = GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
    private final PreparableReloadListener lastVanilla;

    @ApiStatus.Internal
    protected SortedReloadListenerEvent(List<PreparableReloadListener> vanillaListeners, NameLookup lookup) {
        // Register the names for all vanilla listeners
        for (PreparableReloadListener listener : vanillaListeners) {
            ResourceLocation key = lookup.apply(listener);
            this.addListener(key, listener);
        }

        // Setup the edges for vanilla listeners
        for (int i = 1; i < vanillaListeners.size(); i++) {
            PreparableReloadListener prev = vanillaListeners.get(i - 1);
            PreparableReloadListener listener = vanillaListeners.get(i);
            this.graph.putEdge(prev, listener);
        }

        this.lastVanilla = vanillaListeners.getLast();
    }

    /**
     * Adds a new {@link PreparableReloadListener reload listener} to the resource manager.
     * <p>
     * Unless explicitly specified, this listener will run after all vanilla listeners, in the order it was registered.
     * 
     * @param key      The resource location that identifies the reload listener for dependency sorting.
     * @param listener The listener to add.
     * 
     * @throws IllegalArgumentException if another listener with that key was already registered.
     */
    public void addListener(ResourceLocation key, PreparableReloadListener listener) {
        if (this.registry.containsKey(key) || this.registry.containsValue(listener)) {
            throw new IllegalArgumentException("Attempted to register two reload listeners for the same key: " + key);
        }
        this.registry.put(key, listener);
        this.keys.put(listener, key);
        this.graph.addNode(listener);
    }

    /**
     * Adds a new dependency entry, such that {@code first} must run before {@code second}.
     * <p>
     * Introduction of dependency cycles (first->second->first) will cause an error when the event is finished.
     * 
     * @param first  The key of the reload listener that must run first.
     * @param second The key of the reload listener that must run after {@code first}.
     * 
     * @throws IllegalArgumentException if either {@code first} or {@code second} has not been registered via {@link #addListener}.
     * 
     * @see {@link NeoForgeReloadListeners} for Neo's reload listener keys.
     * @see {@link VanillaClientListeners} for the keys of vanilla client listeners.
     * @see {@link VanillaServerListeners} for the keys of vanilla server listeners.
     */
    public void addDependency(ResourceLocation first, ResourceLocation second) {
        this.graph.putEdge(this.getOrThrow(first), this.getOrThrow(second));
    }

    /**
     * Returns an immutable view of the dependency graph.
     */
    public Graph<PreparableReloadListener> getGraph() {
        return this.graph;
    }

    /**
     * Returns an immutable view of the reload listener registry.
     * <p>
     * The registry is linked, meaning the iteration order depends on the registration order.
     */
    public Map<ResourceLocation, PreparableReloadListener> getRegistry() {
        return Collections.unmodifiableMap(this.registry);
    }

    /**
     * Returns a {@link NameLookup} for all reload listeners known by this event.
     */
    public NameLookup getNameLookup() {
        return this::getOrThrow;
    }

    /**
     * Returns a reference to the last vanilla listener, used during the final sort.
     */
    @ApiStatus.Internal
    public PreparableReloadListener getLastVanillaListener() {
        return this.lastVanilla;
    }

    private PreparableReloadListener getOrThrow(ResourceLocation key) {
        PreparableReloadListener listener = this.registry.get(key);
        if (listener == null) {
            throw new IllegalArgumentException("Unknown reload listener: " + key);
        }
        return listener;
    }

    private ResourceLocation getOrThrow(PreparableReloadListener listener) {
        ResourceLocation key = this.keys.get(listener);
        if (key == null) {
            throw new IllegalArgumentException("Unknown reload listener: " + listener);
        }
        return key;
    }

    @FunctionalInterface
    public interface NameLookup extends Function<PreparableReloadListener, ResourceLocation> {
        /**
         * Looks up the name for a reload listener.
         * 
         * @throws IllegalArgumentException if there was no name for the listener.
         */
        @Override
        ResourceLocation apply(PreparableReloadListener t);
    }
}
