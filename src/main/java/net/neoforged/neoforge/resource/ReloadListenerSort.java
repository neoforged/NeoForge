/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.fml.loading.toposort.CyclePresentException;
import net.neoforged.fml.loading.toposort.TopologicalSort;
import net.neoforged.neoforge.event.SortedReloadListenerEvent;
import net.neoforged.neoforge.event.SortedReloadListenerEvent.NameLookup;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ReloadListenerSort {
    /**
     * Sorts the listeners and emits the returned list.
     * <p>
     * This method modifies the current state of the graph to ensure that all dangling listeners run after vanilla.
     * 
     * @return An immutable, sorted list of listeners based on the current dependency graph.
     * 
     * @throws IllegalArgumentException if cycles were detected in the dependency graph.
     */
    public static List<PreparableReloadListener> sort(SortedReloadListenerEvent event) {
        return sortListeners(event.getNameLookup(), (MutableGraph<PreparableReloadListener>) event.getGraph(), event.getRegistry(), event.getLastVanillaListener());
    }

    /**
     * Implementation for {@link #sort(SortedReloadListenerEvent)}.
     * 
     * @param lookup      The {@link SortedReloadListenerEvent#getNameLookup() name lookup} from the event.
     * @param graph       The mutable graph. The event only exposes a non-mutable graph, but we can just downcast it since we know it's a MutableGraph.
     * @param registry    The reload listener registry.
     * @param lastVanilla A reference to the last vanilla listener in vanilla order.
     * @return An immutable, sorted list of listeners based on the current dependency graph.
     * 
     * @throws IllegalArgumentException if cycles were detected in the dependency graph.
     */
    public static List<PreparableReloadListener> sortListeners(NameLookup lookup, MutableGraph<PreparableReloadListener> graph, Map<ResourceLocation, PreparableReloadListener> registry, PreparableReloadListener lastVanilla) {
        // For any entries without a dependency, ensure they depend on the last vanilla loader.
        for (Map.Entry<ResourceLocation, PreparableReloadListener> entry : registry.entrySet()) {
            if (needsToBeLinkedToVanilla(lookup, graph, entry.getValue())) {
                graph.putEdge(lastVanilla, entry.getValue());
            }
        }

        // Then build the index mapping in a way that can be used as a comparator to preserve insertion order.
        Object2IntMap<PreparableReloadListener> insertionOrder = new Object2IntOpenHashMap<>();
        int idx = 0;
        for (PreparableReloadListener listener : registry.values()) {
            insertionOrder.put(listener, idx++);
        }

        // Then do the sort.
        try {
            List<PreparableReloadListener> sorted = TopologicalSort.topologicalSort(graph, Comparator.comparingInt(insertionOrder::getInt));
            return Collections.unmodifiableList(sorted);
        } catch (CyclePresentException ex) {
            // If a cycle is found, we have to transform the information in the exception back into the registered keys.
            Set<Set<PreparableReloadListener>> cycles = ex.getCycles();
            Set<Set<ResourceLocation>> keyedCycles = cycles.stream().map(set -> {
                return set.stream().map(lookup::apply).collect(Collectors.toCollection(LinkedHashSet::new));
            }).collect(Collectors.toSet());

            // Finally, build a real error message and re-throw.
            StringBuilder sb = new StringBuilder();
            sb.append("Cycles were detected during reload listener sorting:").append('\n');

            idx = 0;
            for (Set<ResourceLocation> cycle : keyedCycles) {
                StringBuilder msg = new StringBuilder();

                msg.append(idx++).append(": ");

                for (ResourceLocation key : cycle) {
                    msg.append(key).append("->");
                }

                msg.append(cycle.iterator().next());

                sb.append(msg);
                sb.append('\n');
            }

            throw new IllegalArgumentException(sb.toString());
        }
    }

    /**
     * A node needs to be linked to vanilla if it is otherwise "dangling" from the vanilla graph.
     * <p>
     * To determine if a node needs to be linked, we perform a forward and backward DFS to detect if there are any links to vanilla nodes.
     * If there are no links, we add an edge against the last vanilla listener based on the default order.
     * 
     * @return true if the listener needs to be linked to vanilla.
     */
    private static boolean needsToBeLinkedToVanilla(NameLookup lookup, Graph<PreparableReloadListener> graph, PreparableReloadListener listener) {
        if (isVanilla(lookup, listener)) {
            return false;
        }

        for (PreparableReloadListener node : Traverser.forGraph(graph).depthFirstPreOrder(listener)) {
            if (isVanilla(lookup, node)) {
                return false;
            }
        }

        for (PreparableReloadListener node : Traverser.forGraph(Graphs.transpose(graph)).depthFirstPreOrder(listener)) {
            if (isVanilla(lookup, node)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isVanilla(NameLookup lookup, PreparableReloadListener listener) {
        return "minecraft".equals(lookup.apply(listener).getNamespace());
    }
}
