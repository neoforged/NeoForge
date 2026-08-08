/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.common.CommonHooks;
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
    public static List<PreparableReloadListener> sortListeners(NameLookup lookup, MutableGraph<PreparableReloadListener> graph, Map<Identifier, PreparableReloadListener> registry, PreparableReloadListener lastVanilla) {
        // For any entries without a dependency, ensure they depend on the last vanilla loader.
        for (Map.Entry<Identifier, PreparableReloadListener> entry : registry.entrySet()) {
            if (needsToBeLinkedToVanilla(lookup, graph, entry.getValue())) {
                graph.putEdge(lastVanilla, entry.getValue());
            }
        }

        return CommonHooks.sortGraphChecked(graph, registry.values(), "reload listener", lookup);
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
