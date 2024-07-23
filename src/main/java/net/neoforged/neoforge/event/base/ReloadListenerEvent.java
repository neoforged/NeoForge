/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.loading.toposort.TopologicalSort;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * A base class for reload listener events.
 */
public abstract class ReloadListenerEvent extends Event {
    private final List<PreparableReloadListener> listeners = new ArrayList<>();

    @ApiStatus.Internal
    protected ReloadListenerEvent() {}

    /**
     * Register an unordered reload listener that will be appended to the currently registered listeners.
     *
     * @param id       the ID of the reload listener, used for ordering purposes
     * @param listener the listener to register
     */
    public void addListener(@Nullable ResourceLocation id, PreparableReloadListener listener) {
        addListener(id, listener, List.of(), List.of());
    }

    /**
     * Register a reload listener.
     *
     * @param id              the ID of the reload listener, used for ordering purposes
     * @param listener        the listener to register
     * @param listenersBefore the IDs of the reload listeners that will run before this listener
     * @param listenersAfter  the IDs of the reload listeners that will run after this listener
     */
    public void addListener(@Nullable ResourceLocation id, PreparableReloadListener listener, List<ResourceLocation> listenersBefore, List<ResourceLocation> listenersAfter) {
        listeners.add(id == null ? wrap(listener) : new IdentifiableReloadListener(id, wrap(listener), listenersBefore, listenersAfter));

        if (!listenersBefore.isEmpty()) {
            Preconditions.checkArgument(id != null, "Cannot specify listeners to run before the reload listener " + listener + " as it has no ID");
        }
        if (!listenersAfter.isEmpty()) {
            Preconditions.checkArgument(id != null, "Cannot specify listeners to run after the reload listener " + listener + " as it has no ID");
        }
    }

    /**
     * {@return an unmodifiable view of the currently registered listeners}
     * Note: this view is <b>unsorted</b>.
     */
    @UnmodifiableView
    public List<PreparableReloadListener> getListeners() {
        return ImmutableList.copyOf(listeners);
    }

    /**
     * {@return a sorted copy of the {@code baseListeners} with the {@link #listeners} added}
     */
    public List<PreparableReloadListener> getSortedListeners(List<PreparableReloadListener> baseListeners, Map<Class<? extends PreparableReloadListener>, ResourceLocation> vanillaIds) {
        return sort(baseListeners, listeners, vanillaIds);
    }

    /**
     * Wrap the {@code listener} with additional context.
     */
    protected PreparableReloadListener wrap(PreparableReloadListener listener) {
        return listener;
    }

    @SuppressWarnings("UnstableApiUsage")
    protected static List<PreparableReloadListener> sort(List<PreparableReloadListener> baseListeners, List<PreparableReloadListener> listeners, Map<Class<? extends PreparableReloadListener>, ResourceLocation> vanillaIds) {
        final MutableGraph<PreparableReloadListener> graph = GraphBuilder.directed().expectedNodeCount(baseListeners.size() + listeners.size())
                .nodeOrder(ElementOrder.insertion()).build();

        final Map<ResourceLocation, PreparableReloadListener> byId = new HashMap<>();

        final List<PreparableReloadListener> insertionOrder = new ArrayList<>(baseListeners);
        insertionOrder.addAll(listeners);

        for (PreparableReloadListener baseListener : baseListeners) {
            var id = vanillaIds.get(baseListener.getClass());
            if (id != null) {
                byId.put(id, baseListener);
            }
            graph.addNode(baseListener);
        }

        for (PreparableReloadListener listener : listeners) {
            if (listener instanceof IdentifiableReloadListener identifiable) {
                byId.put(identifiable.id(), identifiable);
            }

            graph.addNode(listener);
        }

        // Add edges between vanilla listeners
        for (int i = 0; i < baseListeners.size(); i++) {
//            if (i < baseListeners.size() - 1) {
//                graph.putEdge(baseListeners.get(i), baseListeners.get(i + 1));
//            }
            if (i > 0) {
                graph.putEdge(baseListeners.get(i - 1), baseListeners.get(i));
            }
        }

        // Add edges requested by modder-added listeners
        for (PreparableReloadListener listener : listeners) {
            if (listener instanceof IdentifiableReloadListener identifiable) {
                if (!identifiable.before.isEmpty() || !identifiable.after.isEmpty()) {
                    for (ResourceLocation before : identifiable.before) {
                        var value = byId.get(before);
                        if (value != null) {
                            graph.putEdge(value, identifiable);
                        }
                    }
                    for (ResourceLocation after : identifiable.after) {
                        var value = byId.get(after);
                        if (value != null) {
                            graph.putEdge(identifiable, value);
                        }
                    }

                    continue;
                }
            }

            // If the listener does not specify any ordering, add it after the last vanilla one
            // TODO - fix
//            graph.putEdge(baseListeners.getLast(), listener);
        }

        return TopologicalSort.topologicalSort(graph, Comparator.comparing(insertionOrder::indexOf));
    }

    /**
     * A {@linkplain PreparableReloadListener} that wraps another listener with an ID for topologically sorting them.
     */
    @ApiStatus.Internal
    public record IdentifiableReloadListener(ResourceLocation id,
            PreparableReloadListener listener, List<ResourceLocation> before, List<ResourceLocation> after) implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller profiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return listener.reload(barrier, manager, profiler, reloadProfiler, backgroundExecutor, gameExecutor);
        }

        @Override
        public String getName() {
            return id.toString();
        }
    }
}
