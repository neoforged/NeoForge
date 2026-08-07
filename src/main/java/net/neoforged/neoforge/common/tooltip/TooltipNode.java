/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// A node in the structured tooltip tree. Structure is expressed by this sealed hierarchy, not by tags:
/// a node is either an {@link Entry} (one line of content) or a {@link Group} (ordered children).
///
/// Identity and semantics live in {@link #metadata()} (tags, provider). Node objects are compared by
/// identity for document positioning; matching for negotiation is done by tag.
public sealed interface TooltipNode permits TooltipNode.Entry, TooltipNode.Group {
    Metadata metadata();

    /// A leaf {@link TooltipNode}: one line of text plus {@link Metadata}.
    /// {@link Component#empty()} is a real, addressable line.
    record Entry(Component component, Metadata metadata) implements TooltipNode {
        public Entry(Component component, Metadata metadata) {
            this.component = Objects.requireNonNull(component, "component");
            this.metadata = Objects.requireNonNull(metadata, "metadata");
        }

        @Override
        public String toString() {
            return "Entry[" + component.getString() + "]";
        }
    }

    /// A {@link TooltipNode} with ordered children. Used both for the auto-created per-appender <em>source
    /// groups</em> and for explicit multi-line blocks authored via {@code output.group(...)}.
    record Group(List<TooltipNode> children, Metadata metadata) implements TooltipNode {
        public Group(List<TooltipNode> children, Metadata metadata) {
            this.children = List.copyOf(children);
            this.metadata = Objects.requireNonNull(metadata, "metadata");
        }

        @Override
        public String toString() {
            return "Group" + children;
        }
    }

    /// The identity and semantics of a {@link TooltipNode}: its plain tags (many allowed), its single negotiated
    /// tag (at most one), the mod that produced the content, and a declaration ordinal used only for deterministic
    /// tie-breaks. Source and data-component identity are expressed as tags (see {@link TooltipTags}), not as
    /// separate fields.
    ///
    /// Immutable. Use {@link #builder()} / {@link #toBuilder()}.
    final class Metadata {
        private final Set<TooltipTag<?, ?>> plainTags;
        @Nullable
        private final TooltipTag<?, ?> negotiatedTag;
        private final String providerModId;
        private final long declarationOrdinal;

        private Metadata(Set<TooltipTag<?, ?>> plainTags, @Nullable TooltipTag<?, ?> negotiatedTag, String providerModId, long declarationOrdinal) {
            this.plainTags = plainTags;
            this.negotiatedTag = negotiatedTag;
            this.providerModId = providerModId;
            this.declarationOrdinal = declarationOrdinal;
        }

        public Set<TooltipTag<?, ?>> plainTags() {
            return plainTags;
        }

        @Nullable
        public TooltipTag<?, ?> negotiatedTag() {
            return negotiatedTag;
        }

        public String providerModId() {
            return providerModId;
        }

        public long declarationOrdinal() {
            return declarationOrdinal;
        }

        /// True if this node carries {@code tag} as a plain or negotiated tag.
        public boolean hasTag(TooltipTag<?, ?> tag) {
            return plainTags.contains(tag) || tag.equals(negotiatedTag);
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder toBuilder() {
            Builder builder = new Builder();
            builder.plainTags.addAll(plainTags);
            builder.negotiatedTag = negotiatedTag;
            builder.providerModId = providerModId;
            builder.declarationOrdinal = declarationOrdinal;
            return builder;
        }

        public static final class Builder {
            private final Set<TooltipTag<?, ?>> plainTags = new LinkedHashSet<>();
            @Nullable
            private TooltipTag<?, ?> negotiatedTag;
            private String providerModId = "minecraft";
            private long declarationOrdinal;

            private Builder() {}

            public Builder providerModId(String providerModId) {
                this.providerModId = providerModId;
                return this;
            }

            public Builder declarationOrdinal(long declarationOrdinal) {
                this.declarationOrdinal = declarationOrdinal;
                return this;
            }

            /// Attaches a tag. Plain tags go into the plain set (many allowed). A negotiated tag is held singly:
            /// attaching a second, different negotiated tag throws {@link IllegalStateException} eagerly.
            public Builder addTag(TooltipTag<?, ?> tag) {
                if (tag.isNegotiated()) {
                    if (negotiatedTag != null && !negotiatedTag.equals(tag)) {
                        throw new IllegalStateException(
                                "A tooltip node may carry at most one negotiated tag; already has " + negotiatedTag + ", cannot add " + tag);
                    }
                    negotiatedTag = tag;
                } else {
                    plainTags.add(tag);
                }
                return this;
            }

            public Metadata build() {
                return new Metadata(Set.copyOf(plainTags), negotiatedTag, providerModId, declarationOrdinal);
            }
        }
    }

    /// Returns a copy of {@code node} with {@code tag} attached to its metadata.
    @ApiStatus.Internal
    static TooltipNode withAddedTag(TooltipNode node, TooltipTag<?, ?> tag) {
        Metadata metadata = node.metadata().toBuilder().addTag(tag).build();
        if (node instanceof Entry entry) {
            return new Entry(entry.component(), metadata);
        }
        return new Group(((Group) node).children(), metadata);
    }

    /// Pre-order DFS over the tree rooted at {@code node}, visiting every node (including groups).
    @ApiStatus.Internal
    static void preOrder(TooltipNode node, Consumer<TooltipNode> sink) {
        sink.accept(node);
        if (node instanceof Group group) {
            for (TooltipNode child : group.children()) {
                preOrder(child, sink);
            }
        }
    }

    /// Flatten to the list of leaf entries in document order.
    @ApiStatus.Internal
    static List<Entry> flatten(List<Group> groups) {
        List<Entry> out = new ArrayList<>();
        for (Group group : groups) {
            preOrder(group, node -> {
                if (node instanceof Entry entry) {
                    out.add(entry);
                }
            });
        }
        return out;
    }
}
