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

/// Resolves a {@link TooltipSnapshot} against a list of {@link TooltipIntent}s into the final list of top-level
/// {@link TooltipGroup}s, in strict listener-fire-independent phases:
///
/// 1. **Candidate** &mdash; negotiated tags resolved by their {@link TooltipResolver}; survivors render where
///    the first candidate stood, non-survivors are dropped.
/// 2. **Modification** &mdash; {@code remove}/{@code replace} targets resolved against the post-candidate
///    working doc (0 matches =&gt; noop, &gt;1 =&gt; diagnostic + drop that intent); at most one intent wins per
///    target node (priority, then Replace&gt;Remove, then provider, then ordinal); atomic, no partial application.
/// 3. **Ordering** &mdash; {@code add} anchors become constraints in a runtime graph sorted by stable document
///    ordinal; cycles broken by dropping the least-preferred constraint.
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
    public static List<TooltipGroup> resolve(TooltipSnapshot snapshot, List<TooltipIntent> intents, Consumer<String> diagnostics) {
        // Phase 1: candidate resolution.
        Map<TooltipNode, List<TooltipNode>> actions = new IdentityHashMap<>();
        resolveCandidates(snapshot, collectPreferences(intents), actions);
        List<TooltipGroup> afterCandidates = applyActions(snapshot.groups(), actions);
        if (afterCandidates.isEmpty()) {
            return List.of();
        }

        // Phase 2: exact-match + modification.
        TooltipSnapshot working = TooltipSnapshot.of(afterCandidates);
        List<TooltipGroup> afterMods = applyActions(afterCandidates, runModifications(working, intents, diagnostics));

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

    private static void resolveCandidates(TooltipSnapshot snapshot, Map<Identifier, List<TooltipResolver.Context.Vote>> preferences, Map<TooltipNode, List<TooltipNode>> actions) {
        for (Identifier id : snapshot.negotiatedTagIds()) {
            var type = snapshot.negotiatedType(id);
            if (type == null) continue;
            List<TooltipNode> candidates = snapshot.candidatesFor(id);
            if (candidates.isEmpty()) continue;
            var context = new TooltipResolver.Context(preferences.getOrDefault(id, List.of()), snapshot.documentOrder());
            List<TooltipNode> survivors = type.resolver().resolve(type, candidates, context);
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

    private static Map<TooltipNode, List<TooltipNode>> runModifications(TooltipSnapshot working, List<TooltipIntent> intents, Consumer<String> diagnostics) {
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
        return TooltipIntentKey.PREFERENCE.compare(a.key(), b.key());
    };

    private static String kind(TooltipIntent intent) {
        return intent instanceof TooltipIntent.Replace ? "replace" : "remove";
    }

    private static String describe(TooltipNode node) {
        if (node instanceof TooltipEntry entry) {
            return "entry '" + entry.component().getString() + "'";
        }
        return "group";
    }

    // ---- Apply actions recursively --------------------------------------------------

    private static List<TooltipGroup> applyActions(List<TooltipGroup> groups, Map<TooltipNode, List<TooltipNode>> actions) {
        List<TooltipGroup> out = new ArrayList<>();
        for (TooltipGroup group : groups) {
            for (TooltipNode node : expand(group, actions)) {
                if (node instanceof TooltipGroup expandedGroup) {
                    out.add(expandedGroup);
                } else if (node instanceof TooltipEntry entry) {
                    // A replaced/stray entry at top level: wrap into a source group to keep structure.
                    out.add(new TooltipGroup(List.of(entry), entry.metadata()));
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
        if (node instanceof TooltipGroup group) {
            List<TooltipNode> children = new ArrayList<>();
            for (TooltipNode child : group.children()) {
                children.addAll(expand(child, actions));
            }
            return List.of(new TooltipGroup(children, group.metadata()));
        }
        return List.of(node);
    }

    // ---- Phase 3: ordering of added content ------------------------------------------

    private record Unit(TooltipGroup group, long ordinal) {
    }

    private record Edge(int from, int to, TooltipIntentKey key, String label) {
    }

    private static List<TooltipGroup> runOrdering(List<TooltipGroup> groups, List<TooltipIntent.Add> adds, Consumer<String> diagnostics) {
        TooltipSnapshot working = TooltipSnapshot.of(groups);

        // Each top-level group is a placement unit with a stable ordinal. Adds become new units at the tail.
        List<Unit> units = new ArrayList<>();
        for (TooltipGroup group : groups) {
            units.add(new Unit(group, units.size()));
        }
        int existingCount = units.size();
        List<Edge> edges = new ArrayList<>();

        for (TooltipIntent.Add add : adds) {
            String provider = add.key().providerId();
            Unit unit = new Unit(
                    new TooltipGroup(toEntries(add.content(), provider), TooltipMetadata.builder().providerModId(provider).build()),
                    existingCount + add.key().declarationOrdinal());

            TooltipTag<?, ?> afterTag = add.after();
            TooltipTag<?, ?> beforeTag = add.before();
            Integer afterUnit = resolveAnchorUnit(working, groups, afterTag);
            Integer beforeUnit = resolveAnchorUnit(working, groups, beforeTag);
            boolean anchorFailed = (afterTag != null && afterUnit == null) || (beforeTag != null && beforeUnit == null);

            if (anchorFailed && add.fallbackTag() != null) {
                Integer fallbackUnit = resolveAnchorUnit(working, groups, add.fallbackTag());
                if (fallbackUnit != null) {
                    if (add.fallbackAfter()) {
                        afterUnit = fallbackUnit;
                        afterTag = add.fallbackTag();
                    } else {
                        beforeUnit = fallbackUnit;
                        beforeTag = add.fallbackTag();
                    }
                    anchorFailed = false;
                }
            }

            if (anchorFailed) {
                if (add.fallback() == TooltipFallback.HEAD) {
                    unit = new Unit(unit.group(), -1L - add.key().declarationOrdinal());
                } else if (add.fallback() != TooltipFallback.TAIL) {
                    diagnostics.accept("dropped: add by '" + provider + "': anchor unresolved and no fallback; intent dropped.");
                    continue;
                }
            } else {
                int addId = units.size();
                if (afterUnit != null) {
                    edges.add(new Edge(afterUnit, addId, add.key(), "after(" + afterTag + ")"));
                }
                if (beforeUnit != null) {
                    edges.add(new Edge(addId, beforeUnit, add.key(), "before(" + beforeTag + ")"));
                }
            }
            units.add(unit);
        }

        List<TooltipGroup> result = new ArrayList<>();
        for (int id : softTopoSort(units, edges, diagnostics)) {
            result.add(units.get(id).group());
        }
        return result;
    }

    /// Resolve a tag to the top-level group containing (or being) the single matched node; null if unresolved/ambiguous.
    private static @Nullable Integer resolveAnchorUnit(TooltipSnapshot working, List<TooltipGroup> groups, @Nullable TooltipTag<?, ?> tag) {
        if (tag == null || working.count(tag) > 1) {
            return null; // absent or ambiguous; caller treats as unresolved -> drop or fallback
        }
        TooltipNode node = working.findFirst(tag);
        if (node == null) {
            return null;
        }
        for (int i = 0; i < groups.size(); i++) {
            if (contains(groups.get(i), node)) {
                return i;
            }
        }
        return null;
    }

    private static boolean contains(TooltipNode haystack, TooltipNode needle) {
        if (haystack == needle) return true;
        if (haystack instanceof TooltipGroup group) {
            for (TooltipNode child : group.children()) {
                if (contains(child, needle)) return true;
            }
        }
        return false;
    }

    // ---- Soft topological sort with cycle breaking ------------------------------------

    private static List<Integer> softTopoSort(List<Unit> units, List<Edge> edges, Consumer<String> diagnostics) {
        List<Edge> active = new ArrayList<>(edges);
        Set<Integer> nodes = new HashSet<>();
        for (int i = 0; i < units.size(); i++) {
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
            PriorityQueue<Integer> ready = new PriorityQueue<>(Comparator.comparingLong(id -> units.get(id).ordinal()));
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
                    if (drop == null || TooltipIntentKey.PREFERENCE.compare(edge.key(), drop.key()) > 0) {
                        drop = edge;
                    }
                }
            }
            if (drop == null) {
                diagnostics.accept("cycle: Ordering cycle could not be resolved; emitting remaining nodes in document order.");
                List<Integer> rest = new ArrayList<>(emitted);
                unemitted.stream().sorted(Comparator.comparingLong(id -> units.get(id).ordinal())).forEach(rest::add);
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
            entries.add(new TooltipEntry(line, TooltipMetadata.builder().providerModId(providerModId).build()));
        }
        return entries;
    }
}
