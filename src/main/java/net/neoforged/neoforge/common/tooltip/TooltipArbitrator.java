/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Resolves a {@link TooltipDocument.Snapshot} against a list of {@link TooltipIntent}s into the final list of
/// top-level {@link TooltipNode.Group}s, in strict listener-fire-independent phases:
///
/// 1. **Candidate** &mdash; negotiated tags resolved by their {@link TooltipResolver}; survivors render where
///    the first candidate stood, non-survivors are dropped.
/// 2. **Modification** &mdash; {@code remove}/{@code replace} targets resolved against the post-candidate
///    working doc (0 matches =&gt; noop, &gt;1 =&gt; diagnostic + drop that intent); at most one intent wins per
///    target node (priority, then Replace&gt;Remove, then provider, then ordinal); atomic, no partial application.
/// 3. **Ordering** &mdash; {@code add} anchors resolve to {@code (parentGroup, anchorChild)}, so content anchored
///    <em>inside</em> a group is inserted as a child of that group (wrapped in its own source group). Sorting runs
///    independently at every level of the tree: existing children keep their relative document order, added units
///    are ordered by the cross-listener {@link TooltipIntent.Key#PREFERENCE} total order, and anchor constraints
///    become edges in a per-level soft topological sort; cycles are broken by dropping the least-preferred
///    constraint.
///
/// Every action is expressed as a replacement node list per node (an empty list drops the node), applied
/// recursively by {@link #applyActions}.
///
/// The public API never exposes this class.
@ApiStatus.Internal
public final class TooltipArbitrator {
    private TooltipArbitrator() {}

    /// @param diagnostics receives human-readable negotiation diagnostics (ambiguous targets, dropped intents,
    ///                    broken cycles)
    public static List<TooltipNode.Group> resolve(TooltipDocument.Snapshot snapshot, List<TooltipIntent> intents, Consumer<String> diagnostics) {
        // Phase 1: candidate resolution.
        Map<TooltipNode, List<TooltipNode>> actions = new IdentityHashMap<>();
        resolveCandidates(snapshot, collectPreferences(intents), actions);
        List<TooltipNode.Group> afterCandidates = applyActions(snapshot.groups(), actions);
        if (afterCandidates.isEmpty()) {
            return List.of();
        }

        // Phase 2: exact-match + modification.
        TooltipDocument.Snapshot working = TooltipDocument.Snapshot.of(afterCandidates);
        List<TooltipNode.Group> afterMods = applyActions(afterCandidates, runModifications(working, intents, diagnostics));

        // Phase 3: ordering of added content.
        List<TooltipIntent.Add> adds = new ArrayList<>();
        for (TooltipIntent intent : intents) {
            if (intent instanceof TooltipIntent.Add add && !add.content().isEmpty()) {
                adds.add(add);
            }
        }
        return adds.isEmpty() ? afterMods : runOrdering(afterMods, adds, diagnostics);
    }

    // ---- Phase 1: candidates -------------------------------------------------------

    private static Map<Identifier, List<TooltipResolver.Context.Vote>> collectPreferences(List<TooltipIntent> intents) {
        Map<Identifier, List<TooltipResolver.Context.Vote>> preferences = new HashMap<>();
        for (TooltipIntent intent : intents) {
            if (intent instanceof TooltipIntent.Prefer prefer) {
                preferences.computeIfAbsent(prefer.negotiatedTagId(), k -> new ArrayList<>())
                        .add(new TooltipResolver.Context.Vote(prefer.provider(), prefer.key().priority(), prefer.key().declarationOrdinal()));
            }
        }
        return preferences;
    }

    private static void resolveCandidates(TooltipDocument.Snapshot snapshot, Map<Identifier, List<TooltipResolver.Context.Vote>> preferences, Map<TooltipNode, List<TooltipNode>> actions) {
        for (Identifier id : snapshot.negotiatedTagIds()) {
            var resolver = snapshot.resolverFor(id);
            if (resolver == null) continue;
            List<TooltipNode> candidates = snapshot.candidatesFor(id);
            if (candidates.isEmpty()) continue;
            var context = new TooltipResolver.Context(preferences.getOrDefault(id, List.of()), snapshot.documentOrder());
            List<TooltipNode> survivors = resolver.resolve(id, candidates, context);
            if (survivors.size() == candidates.size()) {
                Set<TooltipNode> survivorSet = Collections.newSetFromMap(new IdentityHashMap<>());
                survivorSet.addAll(survivors);
                if (survivorSet.containsAll(candidates)) {
                    continue; // keepAll: nothing changes
                }
            }
            // Survivors render where the first candidate stood; the remaining candidates are dropped.
            actions.put(candidates.get(0), List.copyOf(survivors));
            for (int i = 1; i < candidates.size(); i++) {
                actions.put(candidates.get(i), List.of());
            }
        }
    }

    // ---- Phase 2: exact-match + modifications --------------------------------------

    private static Map<TooltipNode, List<TooltipNode>> runModifications(TooltipDocument.Snapshot working, List<TooltipIntent> intents, Consumer<String> diagnostics) {
        // Resolve each remove/replace intent to its target node set, then pick one winner per node.
        Map<TooltipNode, TooltipIntent> winners = new IdentityHashMap<>();
        for (TooltipIntent intent : intents) {
            TooltipTag<?, ?> target;
            TooltipIntent.Selector selector;
            if (intent instanceof TooltipIntent.Remove remove) {
                target = remove.target();
                selector = remove.selector();
            } else if (intent instanceof TooltipIntent.Replace replace) {
                target = replace.target();
                selector = replace.selector();
            } else {
                continue;
            }
            List<TooltipNode> matched = working.nodesFor(target);
            if (matched.isEmpty()) {
                continue; // exact-match 0: silent noop
            }
            if (selector == TooltipIntent.Selector.EXACT && matched.size() > 1) {
                diagnostics.accept("ambiguous: Target " + target + " matched " + matched.size() + " nodes; dropping " + kind(intent)
                        + " by '" + intent.key().providerId() + "'. Use removeAll() or match(tag).first().");
                continue;
            }
            List<TooltipNode> selected = switch (selector) {
                case ALL -> matched;
                case FIRST, EXACT -> List.of(matched.get(0));
                case LAST -> List.of(matched.get(matched.size() - 1));
            };
            for (TooltipNode node : selected) {
                winners.merge(node, intent, (existing, challenger) -> {
                    TooltipIntent winner = MOD_ORDER.compare(challenger, existing) < 0 ? challenger : existing;
                    TooltipIntent loser = winner == challenger ? existing : challenger;
                    diagnostics.accept("conflict: " + describe(node) + ": '" + loser.key().providerId() + "' " + kind(loser)
                            + " lost to '" + winner.key().providerId() + "' " + kind(winner) + ".");
                    return winner;
                });
            }
        }

        Map<TooltipNode, List<TooltipNode>> actions = new IdentityHashMap<>();
        winners.forEach((node, winner) -> {
            if (winner instanceof TooltipIntent.Replace replace) {
                actions.put(node, toEntries(replace.replacement(), winner.key().providerId()));
            } else {
                actions.put(node, List.of());
            }
        });
        return actions;
    }

    /// Modification winner order: priority DESC, Replace before Remove, providerId ASC, ordinal ASC.
    private static final Comparator<TooltipIntent> MOD_ORDER = (a, b) -> {
        int c = Integer.compare(b.key().priority(), a.key().priority());
        if (c != 0) return c;
        c = Boolean.compare(a instanceof TooltipIntent.Remove, b instanceof TooltipIntent.Remove);
        if (c != 0) return c;
        return TooltipIntent.Key.PREFERENCE.compare(a.key(), b.key());
    };

    private static String kind(TooltipIntent intent) {
        return intent instanceof TooltipIntent.Replace ? "replace" : "remove";
    }

    private static String describe(TooltipNode node) {
        if (node instanceof TooltipNode.Entry entry) {
            return "entry '" + entry.component().getString() + "'";
        }
        return "group";
    }

    // ---- Apply actions recursively --------------------------------------------------

    private static List<TooltipNode.Group> applyActions(List<TooltipNode.Group> groups, Map<TooltipNode, List<TooltipNode>> actions) {
        List<TooltipNode.Group> out = new ArrayList<>();
        for (TooltipNode.Group group : groups) {
            for (TooltipNode node : expand(group, actions)) {
                if (node instanceof TooltipNode.Group expandedGroup) {
                    out.add(expandedGroup);
                } else if (node instanceof TooltipNode.Entry entry) {
                    // A replaced/stray entry at top level: wrap into a source group to keep structure.
                    out.add(new TooltipNode.Group(List.of(entry), entry.metadata()));
                }
            }
        }
        return out;
    }

    private static List<TooltipNode> expand(TooltipNode node, Map<TooltipNode, List<TooltipNode>> actions) {
        List<TooltipNode> replacement = actions.get(node);
        if (replacement != null) {
            return replacement;
        }
        if (node instanceof TooltipNode.Group group) {
            List<TooltipNode> children = new ArrayList<>();
            for (TooltipNode child : group.children()) {
                children.addAll(expand(child, actions));
            }
            return List.of(new TooltipNode.Group(children, group.metadata()));
        }
        return List.of(node);
    }

    // ---- Phase 3: ordering of added content ------------------------------------------

    /// A resolved anchor: the matched node ({@code child}) and its direct parent group
    /// ({@code parent} == {@code null} means the top level).
    private record Anchor(@Nullable TooltipNode parent, TooltipNode child) {}

    /// One add placed at one level of the tree. {@code parent} == {@code null} means the top level (root).
    private record Placement(
            TooltipIntent.Add add,
            TooltipNode.Group unit,
            @Nullable TooltipNode parent,
            @Nullable TooltipNode afterChild,
            @Nullable TooltipNode beforeChild,
            boolean headFallback) {}

    private record Edge(int from, int to, TooltipIntent.Key key, String label) {}

    private static List<TooltipNode.Group> runOrdering(List<TooltipNode.Group> groups, List<TooltipIntent.Add> adds, Consumer<String> diagnostics) {
        TooltipDocument.Snapshot working = TooltipDocument.Snapshot.of(groups);

        // Direct-parent map for every node below the top level; absence means "top level".
        IdentityHashMap<TooltipNode, TooltipNode.Group> parents = new IdentityHashMap<>();
        for (TooltipNode.Group group : groups) {
            indexParents(group, parents);
        }

        // Resolve every add to a placement at exactly one level of the tree.
        List<Placement> placements = new ArrayList<>();
        for (TooltipIntent.Add add : adds) {
            String provider = add.key().providerId();
            TooltipNode.Group unit = new TooltipNode.Group(toEntries(add.content(), provider), TooltipNode.Metadata.builder().providerModId(provider).build());

            Anchor afterAnchor = resolveAnchor(working, parents, add.after());
            Anchor beforeAnchor = resolveAnchor(working, parents, add.before());
            boolean anchorFailed = (add.after() != null && afterAnchor == null) || (add.before() != null && beforeAnchor == null);
            // The after/before anchors of one add must live in the same parent group.
            if (afterAnchor != null && beforeAnchor != null && afterAnchor.parent() != beforeAnchor.parent()) {
                diagnostics.accept("irreconcilable: add by '" + provider + "': after(" + add.after() + ") and before(" + add.before()
                        + ") anchors belong to different groups.");
                anchorFailed = true;
            }

            if (anchorFailed && add.fallbackTag() != null) {
                Anchor fallbackAnchor = resolveAnchor(working, parents, add.fallbackTag());
                if (fallbackAnchor != null) {
                    if (add.fallbackAfter()) {
                        afterAnchor = fallbackAnchor;
                    } else {
                        beforeAnchor = fallbackAnchor;
                    }
                    boolean unresolved = (add.after() != null && afterAnchor == null) || (add.before() != null && beforeAnchor == null);
                    boolean mismatched = afterAnchor != null && beforeAnchor != null && afterAnchor.parent() != beforeAnchor.parent();
                    anchorFailed = unresolved || mismatched;
                }
            }

            if (anchorFailed) {
                if (add.fallback() == TooltipNegotiation.Fallback.HEAD) {
                    placements.add(new Placement(add, unit, null, null, null, true));
                } else if (add.fallback() == TooltipNegotiation.Fallback.TAIL) {
                    placements.add(new Placement(add, unit, null, null, null, false));
                } else {
                    diagnostics.accept("dropped: add by '" + provider + "': anchor unresolved and no fallback; intent dropped.");
                }
                continue;
            }

            TooltipNode parent = afterAnchor != null ? afterAnchor.parent() : beforeAnchor != null ? beforeAnchor.parent() : null;
            placements.add(new Placement(add, unit, parent,
                    afterAnchor == null ? null : afterAnchor.child(),
                    beforeAnchor == null ? null : beforeAnchor.child(),
                    false));
        }

        // Every group on the path to a touched level must be rebuilt (records are immutable).
        Set<TooltipNode> dirty = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Placement placement : placements) {
            for (TooltipNode node = placement.parent(); node != null && dirty.add(node); node = parents.get(node)) {
                // walk up to the root
            }
        }

        // Structural rebuild of dirty groups (child order unchanged), tracking old -> new instances so that
        // anchors and parents pointing at rebuilt groups can be translated to the new tree.
        IdentityHashMap<TooltipNode, TooltipNode> rebuilt = new IdentityHashMap<>();
        List<TooltipNode> rootChildren = new ArrayList<>();
        for (TooltipNode.Group group : groups) {
            rootChildren.add(rebuildStructure(group, dirty, rebuilt));
        }

        // Translate placements to the rebuilt tree and group them by (new) parent; null parent = root level.
        // dirtyNew holds the rebuilt instances of every dirty group, so applyLevel can recurse through
        // intermediate groups that have no placements of their own.
        Map<TooltipNode, List<Placement>> byParent = new IdentityHashMap<>();
        List<Placement> rootPlacements = new ArrayList<>();
        for (Placement placement : placements) {
            Placement translated = new Placement(
                    placement.add(),
                    placement.unit(),
                    translate(placement.parent(), rebuilt),
                    translate(placement.afterChild(), rebuilt),
                    translate(placement.beforeChild(), rebuilt),
                    placement.headFallback());
            if (translated.parent() == null) {
                rootPlacements.add(translated);
            } else {
                byParent.computeIfAbsent(translated.parent(), k -> new ArrayList<>()).add(translated);
            }
        }
        Set<TooltipNode> dirtyNew = Collections.newSetFromMap(new IdentityHashMap<>());
        for (TooltipNode group : dirty) {
            dirtyNew.add(rebuilt.get(group));
        }

        List<TooltipNode> newRoot = applyLevel(rootChildren, rootPlacements, byParent, dirtyNew, diagnostics);

        List<TooltipNode.Group> result = new ArrayList<>(newRoot.size());
        for (TooltipNode node : newRoot) {
            result.add((TooltipNode.Group) node);
        }
        return result;
    }

    private static @Nullable TooltipNode translate(@Nullable TooltipNode node, IdentityHashMap<TooltipNode, TooltipNode> rebuilt) {
        return node == null ? null : rebuilt.getOrDefault(node, node);
    }

    private static void indexParents(TooltipNode.Group group, IdentityHashMap<TooltipNode, TooltipNode.Group> parents) {
        for (TooltipNode child : group.children()) {
            parents.put(child, group);
            if (child instanceof TooltipNode.Group childGroup) {
                indexParents(childGroup, parents);
            }
        }
    }

    /// Rebuild {@code group} structurally if it or a descendant received insertions (child order unchanged);
    /// untouched subtrees are returned as-is.
    private static TooltipNode.Group rebuildStructure(TooltipNode.Group group, Set<TooltipNode> dirty, IdentityHashMap<TooltipNode, TooltipNode> rebuilt) {
        if (!dirty.contains(group)) {
            return group;
        }
        List<TooltipNode> children = new ArrayList<>();
        for (TooltipNode child : group.children()) {
            children.add(child instanceof TooltipNode.Group childGroup ? rebuildStructure(childGroup, dirty, rebuilt) : child);
        }
        TooltipNode.Group copy = new TooltipNode.Group(children, group.metadata());
        rebuilt.put(group, copy);
        return copy;
    }

    /// Sort one level and recurse into every dirty child group, whether or not it has placements of its own
    /// (placements may sit several levels down).
    private static List<TooltipNode> applyLevel(List<TooltipNode> children, List<Placement> placements, Map<TooltipNode, List<Placement>> byParent, Set<TooltipNode> dirty, Consumer<String> diagnostics) {
        List<TooltipNode> sorted = new ArrayList<>(sortLevel(children, placements, diagnostics));
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i) instanceof TooltipNode.Group group && dirty.contains(group)) {
                sorted.set(i, new TooltipNode.Group(applyLevel(group.children(), byParent.getOrDefault(group, List.of()), byParent, dirty, diagnostics), group.metadata()));
            }
        }
        return sorted;
    }

    /// Soft-sort one level of the tree. Existing children keep their relative document order; an anchored add is
    /// positioned directly at its anchor (adds sharing one anchor stack in the cross-listener
    /// {@link TooltipIntent.Key#PREFERENCE} total order), unanchored adds go to the tail (head-fallback adds
    /// precede everything), and anchor constraints become edges so contradictory constraints still break as
    /// cycles. Keys are unique fractions of {@code scale}: child {@code i} sits at {@code i * scale}, an add after
    /// anchor {@code a} at {@code a * scale + rank + 1}, an add before anchor {@code b} at
    /// {@code b * scale - rank - 1}.
    private static List<TooltipNode> sortLevel(List<TooltipNode> existing, List<Placement> placements, Consumer<String> diagnostics) {
        if (placements.isEmpty()) {
            return existing;
        }
        List<Placement> ordered = new ArrayList<>(placements);
        ordered.sort((a, b) -> TooltipIntent.Key.PREFERENCE.compare(a.add().key(), b.add().key()));

        int existingCount = existing.size();
        long scale = ordered.size() + 1L;
        int headCount = 0;
        for (Placement placement : ordered) {
            if (placement.headFallback()) headCount++;
        }

        List<TooltipNode> units = new ArrayList<>(existing);
        long[] keys = new long[existingCount + ordered.size()];
        IdentityHashMap<TooltipNode, Integer> indexOf = new IdentityHashMap<>();
        for (int i = 0; i < existingCount; i++) {
            keys[i] = i * scale;
            indexOf.put(existing.get(i), i);
        }

        List<Edge> edges = new ArrayList<>();
        IdentityHashMap<TooltipNode, Integer> afterRanks = new IdentityHashMap<>();
        IdentityHashMap<TooltipNode, Integer> beforeRanks = new IdentityHashMap<>();
        int headRank = 0;
        int tailRank = 0;
        for (Placement placement : ordered) {
            int unitId = units.size();
            units.add(placement.unit());
            if (placement.headFallback()) {
                keys[unitId] = -(scale * headCount) + headRank++;
            } else if (placement.afterChild() != null) {
                int rank = afterRanks.getOrDefault(placement.afterChild(), 0);
                afterRanks.put(placement.afterChild(), rank + 1);
                keys[unitId] = indexOf.get(placement.afterChild()) * scale + rank + 1;
            } else if (placement.beforeChild() != null) {
                int rank = beforeRanks.getOrDefault(placement.beforeChild(), 0);
                beforeRanks.put(placement.beforeChild(), rank + 1);
                keys[unitId] = indexOf.get(placement.beforeChild()) * scale - rank - 1;
            } else {
                keys[unitId] = existingCount * scale + tailRank++;
            }
            if (placement.afterChild() != null) {
                edges.add(new Edge(indexOf.get(placement.afterChild()), unitId, placement.add().key(), "after(" + placement.add().after() + ")"));
            }
            if (placement.beforeChild() != null) {
                edges.add(new Edge(unitId, indexOf.get(placement.beforeChild()), placement.add().key(), "before(" + placement.add().before() + ")"));
            }
        }

        List<TooltipNode> result = new ArrayList<>(units.size());
        for (int id : softTopoSort(units.size(), keys, edges, diagnostics)) {
            result.add(units.get(id));
        }
        return result;
    }

    /// Resolve a tag to the single matched node plus its direct parent group; null if absent, ambiguous, or the
    /// tag is null (ambiguous matches are treated as unresolved by the caller: drop or fallback).
    private static @Nullable Anchor resolveAnchor(TooltipDocument.Snapshot working, IdentityHashMap<TooltipNode, TooltipNode.Group> parents, @Nullable TooltipTag<?, ?> tag) {
        if (tag == null || working.count(tag) > 1) {
            return null;
        }
        TooltipNode node = working.findFirst(tag);
        if (node == null) {
            return null;
        }
        return new Anchor(parents.get(node), node);
    }

    // ---- Soft topological sort with cycle breaking ------------------------------------

    private static List<Integer> softTopoSort(int nodeCount, long[] keys, List<Edge> edges, Consumer<String> diagnostics) {
        List<Edge> active = new ArrayList<>(edges);
        Set<Integer> nodes = new HashSet<>();
        for (int i = 0; i < nodeCount; i++) {
            nodes.add(i);
        }
        while (true) {
            Map<Integer, List<Integer>> successors = new HashMap<>();
            Map<Integer, Integer> indegree = new HashMap<>();
            for (int node : nodes) {
                indegree.put(node, 0);
                successors.put(node, new ArrayList<>());
            }
            for (Edge edge : active) {
                successors.get(edge.from()).add(edge.to());
                indegree.merge(edge.to(), 1, Integer::sum);
            }
            PriorityQueue<Integer> ready = new PriorityQueue<>(Comparator.comparingLong(id -> keys[id]));
            for (int node : nodes) {
                if (indegree.get(node) == 0) {
                    ready.add(node);
                }
            }
            List<Integer> emitted = new ArrayList<>();
            while (!ready.isEmpty()) {
                int node = ready.poll();
                emitted.add(node);
                for (int successor : successors.get(node)) {
                    if (indegree.merge(successor, -1, Integer::sum) == 0) {
                        ready.add(successor);
                    }
                }
            }
            if (emitted.size() == nodes.size()) {
                return emitted;
            }
            // Cycle: drop the least-preferred edge whose endpoints are both still unemitted.
            Set<Integer> unemitted = new HashSet<>(nodes);
            emitted.forEach(unemitted::remove);
            Edge drop = null;
            for (Edge edge : active) {
                if (unemitted.contains(edge.from()) && unemitted.contains(edge.to())) {
                    if (drop == null || TooltipIntent.Key.PREFERENCE.compare(edge.key(), drop.key()) > 0) {
                        drop = edge;
                    }
                }
            }
            if (drop == null) {
                diagnostics.accept("cycle: Ordering cycle could not be resolved; emitting remaining nodes in document order.");
                List<Integer> rest = new ArrayList<>(emitted);
                unemitted.stream().sorted(Comparator.comparingLong(id -> keys[id])).forEach(rest::add);
                return rest;
            }
            active.remove(drop);
            diagnostics.accept("cycle: Ordering cycle broken by dropping constraint '" + drop.label() + "' from '" + drop.key().providerId() + "'.");
        }
    }

    // ---- Helpers ------------------------------------------------------------------------

    private static List<TooltipNode> toEntries(List<Component> lines, String providerModId) {
        List<TooltipNode> entries = new ArrayList<>(lines.size());
        for (Component line : lines) {
            entries.add(new TooltipNode.Entry(line, TooltipNode.Metadata.builder().providerModId(providerModId).build()));
        }
        return entries;
    }
}
