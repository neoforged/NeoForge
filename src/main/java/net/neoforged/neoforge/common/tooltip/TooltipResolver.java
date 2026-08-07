/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Resolves the candidate set of a negotiated tag to survivors. Implementations must be pure functions of
/// {@code (tag, candidates, context)}. Built-in resolvers are exposed via the static factories; custom ones are
/// registered as new channels via {@code TooltipTagType.Negotiated.create(id, nodeType, resolver)} +
/// {@link TooltipTagType#register}.
///
/// Every factory returns a <em>singleton</em>. Sharing the instance is what lets independent mods agree on a
/// convention channel: {@link TooltipTagType#register} accepts a second registration of the same id only when it
/// carries the exact same resolver instance, so a custom resolver used as a convention must also be a shared
/// singleton (e.g. from a common library), never a per-mod lambda or freshly-constructed object.
@ApiStatus.NonExtendable
public interface TooltipResolver {
    List<TooltipNode> resolve(TooltipTagType.Negotiated<?, ?> tag, List<TooltipNode> candidates, Context context);

    /// Context handed to a resolver: {@code prefer(...)} votes plus a stable document-order comparator.
    record Context(List<Vote> preferences, Comparator<TooltipNode> documentOrder) {
        /// One {@code prefer(channel, provider)} vote.
        public record Vote(String providerId, int priority, long declarationOrdinal) {
            /// {@code compare(a, b) < 0} means {@code a} is preferred. Mirrors {@link TooltipIntentKey#PREFERENCE}.
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
        return BuiltIn.KEEP_ALL;
    }

    /// One candidate survives: the most-preferred voted provider, else the first in document order.
    static TooltipResolver chooseOne() {
        return BuiltIn.CHOOSE_ONE;
    }

    /// All candidate groups are merged into a single group preserving document order of children.
    static TooltipResolver mergeGroups() {
        return BuiltIn.MERGE_GROUPS;
    }

    /// The candidate from the lexicographically-smallest provider (then earliest declaration) survives.
    static TooltipResolver highestPriority() {
        return BuiltIn.HIGHEST;
    }

    @ApiStatus.Internal
    final class BuiltIn {
        private BuiltIn() {}

        static final TooltipResolver KEEP_ALL = (_, candidates, _) -> candidates;

        static final TooltipResolver CHOOSE_ONE = (_, candidates, context) -> {
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
        };

        static final TooltipResolver MERGE_GROUPS = (_, candidates, _) -> {
            if (candidates.size() <= 1) {
                return List.copyOf(candidates);
            }
            List<TooltipNode> mergedChildren = new ArrayList<>();
            TooltipGroup firstGroup = null;
            for (TooltipNode node : candidates) {
                if (node instanceof TooltipGroup group) {
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
            return List.of(new TooltipGroup(mergedChildren, firstGroup.metadata()));
        };

        static final TooltipResolver HIGHEST = (_, candidates, _) -> {
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
        };

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
