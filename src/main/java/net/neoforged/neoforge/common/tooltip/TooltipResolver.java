/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Resolves the candidate set of a negotiated channel to survivors. Implementations must be pure functions of
/// {@code (channel, candidates, context)}. Built-in resolvers are exposed via the static factories; custom ones
/// become channels via {@code TooltipTag.negotiated(id, nodeType, resolver)}.
///
/// Every resolver carries a stable {@link #id()}: resolvers with the same id are assumed to have the same
/// semantics, so resolver <em>instance</em> identity never participates in any equality or conflict decision.
/// When independent mods declare the same channel with different resolvers, the declarations are adjudicated
/// deterministically (priority, then explicit preempts/defersTo constraints, then resolver id) instead of
/// crashing &mdash; see {@link TooltipTag}.
public interface TooltipResolver {
    /// Stable semantic identity of this resolver. Two resolvers with the same id must behave identically.
    Identifier id();

    List<TooltipNode> resolve(Identifier channel, List<TooltipNode> candidates, Context context);

    /// Context handed to a resolver: {@code prefer(...)} votes plus a stable document-order comparator.
    record Context(List<Vote> preferences, Comparator<TooltipNode> documentOrder) {
        /// One {@code prefer(channel, provider)} vote.
        public record Vote(String providerId, int priority, long declarationOrdinal) {
            /// {@code compare(a, b) < 0} means {@code a} is preferred. Mirrors {@link TooltipIntent.Key#PREFERENCE}.
            public static final Comparator<Vote> PREFERENCE = (a, b) -> {
                int c = Integer.compare(b.priority(), a.priority());
                if (c != 0) return c;
                c = a.providerId().compareTo(b.providerId());
                if (c != 0) return c;
                return Long.compare(a.declarationOrdinal(), b.declarationOrdinal());
            };
        }
    }

    /// All candidates survive.
    static TooltipResolver keepAll() {
        return Builtin.KEEP_ALL;
    }

    /// One candidate survives: the most-preferred voted provider, else the first in document order.
    static TooltipResolver chooseOne() {
        return Builtin.CHOOSE_ONE;
    }

    /// All candidate groups are merged into a single group preserving document order of children.
    static TooltipResolver mergeGroups() {
        return Builtin.MERGE_GROUPS;
    }

    /// The candidate from the lexicographically-smallest provider (then earliest declaration) survives.
    static TooltipResolver highestPriority() {
        return Builtin.HIGHEST;
    }

    /// The built-in resolvers, each with a fixed {@code neoforge:} id.
    @ApiStatus.Internal
    enum Builtin implements TooltipResolver {
        KEEP_ALL("keep_all") {
            @Override
            public List<TooltipNode> resolve(Identifier channel, List<TooltipNode> candidates, Context context) {
                return candidates;
            }
        },
        CHOOSE_ONE("choose_one") {
            @Override
            public List<TooltipNode> resolve(Identifier channel, List<TooltipNode> candidates, Context context) {
                if (candidates.isEmpty()) {
                    return List.of();
                }
                String winnerProvider = winner(context.preferences());
                if (winnerProvider != null) {
                    for (TooltipNode node : candidates) {
                        if (winnerProvider.equals(node.metadata().providerModId())) {
                            return List.of(node);
                        }
                    }
                }
                return List.of(candidates.getFirst());
            }
        },
        MERGE_GROUPS("merge_groups") {
            @Override
            public List<TooltipNode> resolve(Identifier channel, List<TooltipNode> candidates, Context context) {
                if (candidates.size() <= 1) {
                    return List.copyOf(candidates);
                }
                List<TooltipNode> mergedChildren = new ArrayList<>();
                TooltipNode.Group firstGroup = null;
                for (TooltipNode node : candidates) {
                    if (node instanceof TooltipNode.Group group) {
                        if (firstGroup == null) {
                            firstGroup = group;
                        }
                        mergedChildren.addAll(group.children());
                    } else {
                        mergedChildren.add(node);
                    }
                }
                if (firstGroup == null) {
                    return List.copyOf(candidates);
                }
                return List.of(new TooltipNode.Group(mergedChildren, firstGroup.metadata()));
            }
        },
        HIGHEST("highest_priority") {
            @Override
            public List<TooltipNode> resolve(Identifier channel, List<TooltipNode> candidates, Context context) {
                if (candidates.isEmpty()) {
                    return List.of();
                }
                TooltipNode best = candidates.get(0);
                for (TooltipNode node : candidates) {
                    String provider = node.metadata().providerModId();
                    String bestProvider = best.metadata().providerModId();
                    if (provider.compareTo(bestProvider) < 0
                            || (provider.equals(bestProvider) && node.metadata().declarationOrdinal() < best.metadata().declarationOrdinal())) {
                        best = node;
                    }
                }
                return List.of(best);
            }
        };

        private final Identifier id;

        Builtin(String path) {
            this.id = Identifier.fromNamespaceAndPath("neoforge", path);
        }

        @Override
        public Identifier id() {
            return id;
        }

        private static @Nullable String winner(List<Context.Vote> preferences) {
            Context.Vote best = null;
            for (Context.Vote vote : preferences) {
                if (best == null || Context.Vote.PREFERENCE.compare(vote, best) < 0) {
                    best = vote;
                }
            }
            return best == null ? null : best.providerId();
        }
    }
}
