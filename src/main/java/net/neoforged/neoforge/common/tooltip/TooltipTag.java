/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/// A tag attached to a {@link TooltipNode}: an {@link #id()}, the node type it attaches to, an optional
/// {@link TooltipResolver}, and an optional value. A tag with {@code resolver == null} is <em>plain</em> (locate
/// only, all matches coexist); a tag with a resolver opens a <em>negotiated channel</em> whose candidate set is
/// resolved by the resolver.
///
/// Equality is {@code (id, nodeType, value)} &mdash; the resolver is deliberately ignored so that two
/// independently-declared {@code c:} channel tags still match the same nodes.
///
/// The value {@code V} is <em>per-node payload</em> (which claimant, which lore line), never part of the channel
/// definition: channel identity and adjudication only ever see {@code (id, nodeType, resolver.id())}. Two mods
/// attaching different values &mdash; or even different value types &mdash; to the same channel cannot conflict;
/// their candidates are grouped by channel id and resolved value-agnostically.
///
/// ## Channel declarations and conflict adjudication
///
/// Every factory call <em>declares</em> its channel in a global registry; declaration never throws. When one
/// channel is declared with several different resolver ids, the declarations are adjudicated deterministically
/// (independent of declaration or class-load order):
///
/// 1. Declarations sharing a resolver id are merged (max priority, union of preempts/defersTo); a single merged
///    declaration wins directly.
/// 2. Edges: {@code preempts(x)} with {@code x} present adds {@code me -> x}; {@code defersTo(x)} with {@code x}
///    present adds {@code x -> me}; constraints pointing at absent resolver ids are ignored.
/// 3. Kahn topological sort; the ready queue pops by priority DESC, then resolver id ASC. Explicit constraints
///    (preempts/defersTo) beat priority.
/// 4. Cycles (mutual preempts, preempts vs defersTo) are broken by dropping the edge contributed by the
///    least-preferred declaration (lower priority, then lexicographically larger resolver id) and re-running.
/// 5. The first emitted declaration wins; the channel is warned about once. The result is cached and recomputed
///    when a new declaration arrives.
///
/// A channel declared with inconsistent node types, or used as both plain and negotiated, is likewise tolerated
/// with a one-time warning. A channel with no declaration falls back to the resolver of its first candidate.
///
/// @param <N> the node type this tag attaches to
/// @param <V> the value type carried by this tag (use {@link Void} for marker tags)
public record TooltipTag<N extends TooltipNode, V>(
        Identifier id,
        Class<N> nodeType,
        @Nullable TooltipResolver resolver,
        @Nullable V value) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public TooltipTag {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(nodeType, "nodeType");
    }

    /// Declare and create a plain tag type: locate only, all matches coexist.
    public static <N extends TooltipNode, V> TooltipTag<N, V> plain(Identifier id, Class<N> nodeType) {
        declare(id, nodeType, null, 0, List.of(), List.of());
        return new TooltipTag<>(id, nodeType, null, null);
    }

    /// Declare and create a negotiated channel with default priority and no ordering constraints.
    public static <N extends TooltipNode, V> TooltipTag<N, V> negotiated(Identifier id, Class<N> nodeType, TooltipResolver resolver) {
        return negotiated(id, nodeType, resolver, 0);
    }

    /// Declare and create a negotiated channel. Higher {@code priority} declarations are preferred when the
    /// channel is declared with conflicting resolver ids.
    public static <N extends TooltipNode, V> TooltipTag<N, V> negotiated(Identifier id, Class<N> nodeType, TooltipResolver resolver, int priority) {
        return negotiated(id, nodeType, resolver, priority, List.of(), List.of());
    }

    /// Declare and create a negotiated channel with explicit ordering constraints against other resolver ids:
    /// {@code preempts(x)} means "when {@code x} is present, I rank before it"; {@code defersTo(x)} means "when
    /// {@code x} is present, it ranks before me".
    public static <N extends TooltipNode, V> TooltipTag<N, V> negotiated(
            Identifier id,
            Class<N> nodeType,
            TooltipResolver resolver,
            int priority,
            Collection<Identifier> preempts,
            Collection<Identifier> defersTo) {
        Objects.requireNonNull(resolver, "resolver");
        declare(id, nodeType, resolver, priority, preempts, defersTo);
        return new TooltipTag<>(id, nodeType, resolver, null);
    }

    /// Convenience for {@code resolver() != null}.
    public boolean isNegotiated() {
        return resolver != null;
    }

    /// Bind a value to this tag, producing the concrete tag attached to nodes.
    public <T> TooltipTag<N, T> tag(@Nullable T value) {
        return new TooltipTag<>(id, nodeType, resolver, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TooltipTag<?, ?> other)) return false;
        return id.equals(other.id)
                && nodeType.equals(other.nodeType)
                && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nodeType, value);
    }

    @Override
    public String toString() {
        return "Tag(" + id + "=" + value + ")";
    }

    // ---- Channel declaration registry + adjudication -----------------------------------

    private static final Object LOCK = new Object();
    private static final Map<Identifier, List<Declaration>> DECLARATIONS = new HashMap<>();
    /// Adjudication cache; may store {@code null} results, so always probe with {@code containsKey}.
    private static final Map<Identifier, TooltipResolver> ADJUDICATED = new HashMap<>();
    private static final Set<Identifier> WARNED_CONFLICT = new HashSet<>();
    private static final Set<Identifier> WARNED_NODE_TYPE = new HashSet<>();
    private static final Set<Identifier> WARNED_MIXED_KIND = new HashSet<>();

    /// One factory-time channel declaration.
    private record Declaration(
            Class<? extends TooltipNode> nodeType,
            @Nullable TooltipResolver resolver,
            int priority,
            Set<Identifier> preempts,
            Set<Identifier> defersTo) {}

    /// All declarations of one channel that share a resolver id, merged: max priority, union of constraints.
    private static final class MergedDeclaration {
        final TooltipResolver resolver;
        int priority;
        final Set<Identifier> preempts = new LinkedHashSet<>();
        final Set<Identifier> defersTo = new LinkedHashSet<>();

        MergedDeclaration(TooltipResolver resolver, int priority) {
            this.resolver = resolver;
            this.priority = priority;
        }

        Identifier resolverId() {
            return resolver.id();
        }
    }

    /// Declaration preference: higher priority first, then lexicographically smaller resolver id.
    private static final Comparator<MergedDeclaration> PREFERENCE = (a, b) -> {
        int c = Integer.compare(b.priority, a.priority);
        if (c != 0) return c;
        return a.resolverId().compareTo(b.resolverId());
    };

    private record Edge(Identifier from, Identifier to, Identifier contributor, String label) {}

    private static void declare(Identifier id, Class<? extends TooltipNode> nodeType, @Nullable TooltipResolver resolver, int priority, Collection<Identifier> preempts, Collection<Identifier> defersTo) {
        synchronized (LOCK) {
            DECLARATIONS.computeIfAbsent(id, k -> new ArrayList<>())
                    .add(new Declaration(nodeType, resolver, priority, Set.copyOf(preempts), Set.copyOf(defersTo)));
            ADJUDICATED.remove(id);
        }
    }

    /// The resolver governing channel {@code id} after adjudicating its declarations, or {@code null} if the
    /// channel was never declared negotiated.
    static @Nullable TooltipResolver resolverFor(Identifier id) {
        synchronized (LOCK) {
            if (ADJUDICATED.containsKey(id)) {
                return ADJUDICATED.get(id);
            }
            TooltipResolver result = adjudicate(id);
            ADJUDICATED.put(id, result);
            return result;
        }
    }

    private static @Nullable TooltipResolver adjudicate(Identifier channel) {
        List<Declaration> declarations = DECLARATIONS.get(channel);
        if (declarations == null || declarations.isEmpty()) {
            return null;
        }
        List<Declaration> negotiated = declarations.stream().filter(d -> d.resolver() != null).toList();
        if (negotiated.size() != declarations.size() && !negotiated.isEmpty() && WARNED_MIXED_KIND.add(channel)) {
            LOGGER.warn("Tooltip channel '{}' is declared as both plain and negotiated; each kind matches by its own structure.", channel);
        }
        if (negotiated.isEmpty()) {
            return null;
        }
        Class<? extends TooltipNode> firstNodeType = declarations.getFirst().nodeType();
        for (Declaration declaration : declarations) {
            if (!declaration.nodeType().equals(firstNodeType) && WARNED_NODE_TYPE.add(channel)) {
                LOGGER.warn("Tooltip channel '{}' is declared with inconsistent node types ({} vs {}); keeping the first declaration's type.",
                        channel, firstNodeType.getSimpleName(), declaration.nodeType().getSimpleName());
                break;
            }
        }

        // Merge declarations that share a resolver id: max priority, union of constraints.
        Map<Identifier, MergedDeclaration> merged = new LinkedHashMap<>();
        for (Declaration declaration : negotiated) {
            var mergedDeclaration = merged.computeIfAbsent(declaration.resolver().id(),
                    k -> new MergedDeclaration(declaration.resolver(), declaration.priority()));
            mergedDeclaration.priority = Math.max(mergedDeclaration.priority, declaration.priority());
            mergedDeclaration.preempts.addAll(declaration.preempts());
            mergedDeclaration.defersTo.addAll(declaration.defersTo());
        }
        if (merged.size() == 1) {
            return merged.values().iterator().next().resolver;
        }

        // Deterministic declaration order: priority DESC, resolver id ASC.
        List<MergedDeclaration> sorted = new ArrayList<>(merged.values());
        sorted.sort(PREFERENCE);

        List<Edge> active = new ArrayList<>();
        for (MergedDeclaration declaration : sorted) {
            for (Identifier target : declaration.preempts) {
                if (merged.containsKey(target)) {
                    active.add(new Edge(declaration.resolverId(), target, declaration.resolverId(), "preempts(" + target + ")"));
                }
            }
            for (Identifier target : declaration.defersTo) {
                if (merged.containsKey(target)) {
                    active.add(new Edge(target, declaration.resolverId(), declaration.resolverId(), "defersTo(" + target + ")"));
                }
            }
        }

        List<Edge> droppedEdges = new ArrayList<>();
        List<Identifier> emitted;
        while (true) {
            emitted = kahn(sorted, active);
            if (emitted.size() == sorted.size()) {
                break;
            }
            // Cycle: drop the edge contributed by the least-preferred declaration among the unemitted.
            Set<Identifier> unemitted = new HashSet<>(merged.keySet());
            emitted.forEach(unemitted::remove);
            Edge drop = null;
            for (Edge edge : active) {
                if (unemitted.contains(edge.from()) && unemitted.contains(edge.to())) {
                    if (drop == null || PREFERENCE.compare(merged.get(edge.contributor()), merged.get(drop.contributor())) > 0) {
                        drop = edge;
                    }
                }
            }
            if (drop == null) {
                break; // defensive: an unemitted remainder always has an internal edge
            }
            active.remove(drop);
            droppedEdges.add(drop);
        }

        Identifier winnerId = emitted.getFirst();
        if (WARNED_CONFLICT.add(channel)) {
            LOGGER.warn("Tooltip channel '{}' has conflicting resolver declarations {}; dropped edges {}; winner: '{}'.",
                    channel, sorted.stream().map(d -> d.resolverId().toString()).toList(), droppedEdges, winnerId);
        }
        return merged.get(winnerId).resolver;
    }

    /// Kahn's algorithm; the ready queue pops by declaration preference (priority DESC, resolver id ASC).
    private static List<Identifier> kahn(List<MergedDeclaration> declarations, List<Edge> edges) {
        Map<Identifier, List<Identifier>> successors = new HashMap<>();
        Map<Identifier, Integer> indegree = new HashMap<>();
        Map<Identifier, MergedDeclaration> byId = new HashMap<>();
        for (MergedDeclaration declaration : declarations) {
            byId.put(declaration.resolverId(), declaration);
            successors.put(declaration.resolverId(), new ArrayList<>());
            indegree.put(declaration.resolverId(), 0);
        }
        for (Edge edge : edges) {
            successors.get(edge.from()).add(edge.to());
            indegree.merge(edge.to(), 1, Integer::sum);
        }
        PriorityQueue<Identifier> ready = new PriorityQueue<>(Comparator.comparing(byId::get, PREFERENCE));
        for (MergedDeclaration declaration : declarations) {
            if (indegree.get(declaration.resolverId()) == 0) {
                ready.add(declaration.resolverId());
            }
        }
        List<Identifier> emitted = new ArrayList<>();
        while (!ready.isEmpty()) {
            Identifier node = ready.poll();
            emitted.add(node);
            for (Identifier successor : successors.get(node)) {
                if (indegree.merge(successor, -1, Integer::sum) == 0) {
                    ready.add(successor);
                }
            }
        }
        return emitted;
    }
}
