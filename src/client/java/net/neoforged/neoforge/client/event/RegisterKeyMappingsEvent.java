/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.loading.toposort.CyclePresentException;
import net.neoforged.fml.loading.toposort.TopologicalSort;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom {@link KeyMapping key mappings} and {@link KeyMapping.Category key mapping categories}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public class RegisterKeyMappingsEvent extends Event implements IModBusEvent {
    private final Options options;
    private final List<KeyMapping.Category> categories;
    private final Set<KeyMapping.Category> vanillaCategories;
    private final Map<ResourceLocation, KeyMapping.Category> categoriesById = new HashMap<>();
    private final MutableGraph<KeyMapping.Category> graph = GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();

    @ApiStatus.Internal
    public RegisterKeyMappingsEvent(Options options, List<KeyMapping.Category> categories) {
        this.options = options;
        this.categories = categories;
        this.vanillaCategories = Set.copyOf(categories);
        for (KeyMapping.Category category : categories) {
            this.categoriesById.put(category.id(), category);
            this.graph.addNode(category);
        }
        for (int i = 1; i < categories.size() - 1; i++) {
            KeyMapping.Category category = categories.get(i);
            this.graph.putEdge(categories.get(i - 1), category);
            this.graph.putEdge(category, categories.get(i + 1));
        }
    }

    /**
     * Register a new key mapping.
     */
    public void register(KeyMapping key) {
        options.keyMappings = ArrayUtils.add(options.keyMappings, key);
    }

    /**
     * Register a new key mapping category.
     */
    public void registerCategory(KeyMapping.Category category) {
        if (this.categoriesById.putIfAbsent(category.id(), category) != null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "KeyMapping.Category '%s' is already registered.", category.id()));
        }
        this.graph.addNode(category);
    }

    /**
     * Adds a new dependency entry, such that {@code first} must be placed before {@code second} in the controls screen.
     * <p>
     * Introduction of dependency cycles (first->second->first) will cause an error when the event is finished.
     *
     * @param first  The key of the category that must be placed first.
     * @param second The key of the category that must be placed after {@code first}.
     *
     * @throws IllegalArgumentException if either {@code first} or {@code second} has not been registered via {@link #registerCategory}.
     */
    public void addCategoryDependency(ResourceLocation first, ResourceLocation second) {
        KeyMapping.Category firstCat = this.categoriesById.get(first);
        if (firstCat == null) {
            throw new IllegalArgumentException("Unknown KeyMapping.Category: " + first);
        }
        KeyMapping.Category secondCat = this.categoriesById.get(second);
        if (secondCat == null) {
            throw new IllegalArgumentException("Unknown KeyMapping.Category: " + second);
        }

        this.graph.putEdge(firstCat, secondCat);
    }

    @ApiStatus.Internal
    public void sortAndStoreCategories() {
        if (this.categoriesById.size() == this.categories.size()) {
            return;
        }

        // For any entries without a dependency, ensure they depend on the last vanilla category.
        KeyMapping.Category lastVanilla = this.categories.getLast();
        for (KeyMapping.Category category : this.categoriesById.values()) {
            if (needsToBeLinkedToVanilla(category)) {
                this.graph.putEdge(lastVanilla, category);
            }
        }

        // Then do the sort.
        try {
            List<KeyMapping.Category> sorted = TopologicalSort.topologicalSort(
                    this.graph,
                    Comparator.comparing(KeyMapping.Category::id, ResourceLocation::compareNamespaced));
            this.categories.clear();
            this.categories.addAll(sorted);
        } catch (CyclePresentException e) {
            // If a cycle is found, we have to transform the information in the exception back into the registered keys.
            Set<Set<KeyMapping.Category>> cycles = e.getCycles();
            Set<Set<ResourceLocation>> keyedCycles = cycles.stream().map(set -> {
                return set.stream().map(KeyMapping.Category::id).collect(Collectors.toCollection(LinkedHashSet::new));
            }).collect(Collectors.toSet());

            // Finally, build a real error message and re-throw.
            StringBuilder sb = new StringBuilder();
            sb.append("Cycles were detected during key mapping category sorting:").append('\n');

            int idx = 0;
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

    private boolean needsToBeLinkedToVanilla(KeyMapping.Category category) {
        if (isVanilla(category)) {
            return false;
        }

        for (KeyMapping.Category node : Traverser.forGraph(graph).depthFirstPreOrder(category)) {
            if (isVanilla(node)) {
                return false;
            }
        }

        for (KeyMapping.Category node : Traverser.forGraph(Graphs.transpose(graph)).depthFirstPreOrder(category)) {
            if (isVanilla(node)) {
                return false;
            }
        }

        return true;
    }

    private boolean isVanilla(KeyMapping.Category category) {
        return this.vanillaCategories.contains(category);
    }
}
