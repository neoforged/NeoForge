/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// Immutable, query-only view over a frozen {@link TooltipDocument}. This is the single object every negotiation
/// listener reads; it is never mutated. Node ordinals are assigned once here (pre-order DFS) and are part of the
/// immutable snapshot, so {@code match(tag).first()} and resolver ordering are listener-fire-independent.
public final class TooltipSnapshot {
    private final List<TooltipGroup> groups;
    private final Map<TooltipTag<?, ?>, List<TooltipNode>> tagIndex = new HashMap<>();
    private final Map<TooltipNode, Integer> ordinals = new IdentityHashMap<>();
    private final Map<Identifier, List<TooltipNode>> negotiatedById = new LinkedHashMap<>();

    TooltipSnapshot(List<TooltipGroup> groups) {
        this.groups = List.copyOf(groups);
        int[] counter = { 0 };
        for (TooltipGroup group : this.groups) {
            TooltipNodes.preOrder(group, node -> {
                ordinals.put(node, counter[0]++);
                indexTags(node);
            });
        }
    }

    private void indexTags(TooltipNode node) {
        for (TooltipTag<?, ?> tag : node.metadata().plainTags()) {
            tagIndex.computeIfAbsent(tag, t -> new ArrayList<>()).add(node);
        }
        var negotiated = node.metadata().negotiatedTag();
        if (negotiated != null) {
            tagIndex.computeIfAbsent(negotiated, t -> new ArrayList<>()).add(node);
            negotiatedById.computeIfAbsent(negotiated.id(), k -> new ArrayList<>()).add(node);
        }
    }

    public List<TooltipGroup> groups() {
        return groups;
    }

    /// Re-indexing entry point (used by the {@code TooltipArbitrator} to snapshot a working document).
    public static TooltipSnapshot of(List<TooltipGroup> groups) {
        return new TooltipSnapshot(groups);
    }

    /// All nodes carrying {@code tag} (any depth), in document order. Empty if none.
    public List<TooltipNode> nodesFor(TooltipTag<?, ?> tag) {
        var list = tagIndex.get(tag);
        return list == null ? List.of() : Collections.unmodifiableList(list);
    }

    public int count(TooltipTag<?, ?> tag) {
        var list = tagIndex.get(tag);
        return list == null ? 0 : list.size();
    }

    /// The first node carrying {@code tag} in document order, or {@code null}.
    public @Nullable TooltipNode findFirst(TooltipTag<?, ?> tag) {
        var list = tagIndex.get(tag);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    /// All nodes carrying a negotiated tag of the given type (id), regardless of value, in document order.
    /// This is the candidate set a resolver sees.
    public List<TooltipNode> candidatesFor(Identifier negotiatedTagId) {
        var list = negotiatedById.get(negotiatedTagId);
        return list == null ? List.of() : Collections.unmodifiableList(list);
    }

    /// All negotiated-tag ids present in the snapshot.
    public Set<Identifier> negotiatedTagIds() {
        return Collections.unmodifiableSet(negotiatedById.keySet());
    }

    /// The negotiated tag type governing the channel {@code id}, or {@code null}. The registered type wins (a
    /// convention channel's resolver is a global decision, not a function of document order); a channel that was
    /// never registered falls back to the type carried by its first candidate.
    public TooltipTagType.@Nullable Negotiated<?, ?> negotiatedType(Identifier id) {
        var registered = TooltipTagType.REGISTRY.get(id);
        if (registered instanceof TooltipTagType.Negotiated<?, ?> negotiated) {
            return negotiated;
        }
        var list = negotiatedById.get(id);
        if (list == null || list.isEmpty()) {
            return null;
        }
        var tag = list.get(0).metadata().negotiatedTag();
        return tag == null ? null : (TooltipTagType.Negotiated<?, ?>) tag.type();
    }

    public int documentOrdinal(TooltipNode node) {
        Integer ordinal = ordinals.get(node);
        return ordinal == null ? Integer.MAX_VALUE : ordinal;
    }

    /// Comparator ordering nodes by their immutable document ordinal.
    public Comparator<TooltipNode> documentOrder() {
        return Comparator.comparingInt(this::documentOrdinal);
    }

    /// Flatten to leaf entries in document order.
    public List<TooltipEntry> flatten() {
        return TooltipNodes.flatten(groups);
    }
}
