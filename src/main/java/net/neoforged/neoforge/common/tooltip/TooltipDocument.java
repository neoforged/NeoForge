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
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// Mutable builder for a tooltip during appender execution. Top-level {@link TooltipNode.Group}s are held in
/// appender order. Call {@link #freeze()} to obtain the immutable {@link Snapshot} all negotiation listeners
/// read.
public final class TooltipDocument {
    private final List<TooltipNode.Group> groups = new ArrayList<>();
    private long ordinalCounter;

    /// Open a structured output owned by {@code providerModId}, bound to a fresh source group.
    public Output newOutput(String providerModId) {
        return new Output(providerModId, this::nextOrdinal);
    }

    /// Finalize an output into a top-level source group.
    public void addSourceGroup(Output output) {
        groups.add(output.toSourceGroup());
    }

    /// Add a pre-built top-level group.
    public void addGroup(TooltipNode.Group group) {
        groups.add(group);
    }

    public Snapshot freeze() {
        return new Snapshot(groups);
    }

    private long nextOrdinal() {
        return ordinalCounter++;
    }

    /// The structured output an appender writes into. Children accumulate into a source group; tags attach via the
    /// fluent {@link Taggable} returned by {@link #add} / {@link #group}. Authors who don't care about negotiation
    /// simply call {@code output.add(component)} &mdash; no id or tag is required.
    ///
    /// ```java
    /// output.add(description);
    /// output.add(energy).tag(ModTooltipTags.ENERGY);
    /// output.group(ModTooltipTags.ENERGY, group -> {
    ///     group.add(title);
    ///     group.add(details);
    /// });
    /// ```
    public static final class Output {
        private final List<TooltipNode> children = new ArrayList<>();
        private final List<TooltipTag<?, ?>> sourceTags = new ArrayList<>();
        private final String providerModId;
        private final LongSupplier ordinals;

        Output(String providerModId, LongSupplier ordinals) {
            this.providerModId = providerModId;
            this.ordinals = ordinals;
        }

        /// Add one line of content.
        public Taggable add(Component component) {
            children.add(new TooltipNode.Entry(component, childMetadata()));
            return new Taggable(this, children.size() - 1);
        }

        /// Add a multi-line block as a real {@link TooltipNode.Group}.
        public Taggable group(Consumer<Output> consumer) {
            Output child = new Output(providerModId, ordinals);
            consumer.accept(child);
            children.add(new TooltipNode.Group(child.children, childMetadata()));
            return new Taggable(this, children.size() - 1);
        }

        /// Shorthand for {@code group(consumer).tag(tag)}: tag the block as a whole, not every line.
        public Taggable group(TooltipTag<?, ?> tag, Consumer<Output> consumer) {
            return group(consumer).tag(tag);
        }

        /// Attach a plain tag to the source group built by {@link #toSourceGroup()} (used by the lifecycle bridge
        /// to stamp appender/component identity so an author's un-tagged output is still addressable).
        public Output sourceTag(TooltipTag<?, ?> tag) {
            sourceTags.add(tag);
            return this;
        }

        private void tagAt(int index, TooltipTag<?, ?> tag) {
            children.set(index, TooltipNode.withAddedTag(children.get(index), tag));
        }

        private TooltipNode.Metadata childMetadata() {
            return TooltipNode.Metadata.builder()
                    .providerModId(providerModId)
                    .declarationOrdinal(ordinals.getAsLong())
                    .build();
        }

        TooltipNode.Group toSourceGroup() {
            var metadata = childMetadata().toBuilder();
            for (TooltipTag<?, ?> tag : sourceTags) {
                metadata.addTag(tag);
            }
            return new TooltipNode.Group(children, metadata.build());
        }

        /// Fluent handle returned by {@link #add}/{@link #group} for attaching a tag to the just-added node.
        public static final class Taggable {
            private final Output output;
            private final int index;

            private Taggable(Output output, int index) {
                this.output = output;
                this.index = index;
            }

            /// Attach a tag (plain or negotiated) to the node captured by this handle.
            public Taggable tag(TooltipTag<?, ?> tag) {
                output.tagAt(index, tag);
                return this;
            }
        }
    }

    /// Immutable, query-only view over a frozen {@link TooltipDocument}. This is the single object every
    /// negotiation listener reads; it is never mutated. Node ordinals are assigned once here (pre-order DFS) and
    /// are part of the immutable snapshot, so {@code match(tag).first()} and resolver ordering are
    /// listener-fire-independent.
    public static final class Snapshot {
        private final List<TooltipNode.Group> groups;
        private final Map<TooltipTag<?, ?>, List<TooltipNode>> tagIndex = new HashMap<>();
        private final Map<TooltipNode, Integer> ordinals = new IdentityHashMap<>();
        private final Map<Identifier, List<TooltipNode>> negotiatedById = new LinkedHashMap<>();

        Snapshot(List<TooltipNode.Group> groups) {
            this.groups = List.copyOf(groups);
            int[] counter = { 0 };
            for (TooltipNode.Group group : this.groups) {
                TooltipNode.preOrder(group, node -> {
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

        public List<TooltipNode.Group> groups() {
            return groups;
        }

        /// Re-indexing entry point (used by the {@code TooltipArbitrator} to snapshot a working document).
        public static Snapshot of(List<TooltipNode.Group> groups) {
            return new Snapshot(groups);
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

        /// All nodes carrying a negotiated tag of the given channel (id), regardless of value, in document order.
        /// This is the candidate set a resolver sees.
        public List<TooltipNode> candidatesFor(Identifier negotiatedTagId) {
            var list = negotiatedById.get(negotiatedTagId);
            return list == null ? List.of() : Collections.unmodifiableList(list);
        }

        /// All negotiated-channel ids present in the snapshot.
        public Set<Identifier> negotiatedTagIds() {
            return Collections.unmodifiableSet(negotiatedById.keySet());
        }

        /// The resolver governing the channel {@code id}, or {@code null}. The adjudicated declaration wins (a
        /// convention channel's resolver is a global decision, not a function of document order); a channel that
        /// was never declared falls back to the resolver carried by its first candidate.
        public @Nullable TooltipResolver resolverFor(Identifier id) {
            var adjudicated = TooltipTag.resolverFor(id);
            if (adjudicated != null) {
                return adjudicated;
            }
            var list = negotiatedById.get(id);
            if (list == null || list.isEmpty()) {
                return null;
            }
            var tag = list.get(0).metadata().negotiatedTag();
            return tag == null ? null : tag.resolver();
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
        public List<TooltipNode.Entry> flatten() {
            return TooltipNode.flatten(groups);
        }
    }
}
