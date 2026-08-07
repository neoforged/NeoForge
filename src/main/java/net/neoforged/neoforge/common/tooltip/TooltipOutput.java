/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import net.minecraft.network.chat.Component;

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
public final class TooltipOutput {
    private final List<TooltipNode> children = new ArrayList<>();
    private final List<TooltipTag<?, ?>> sourceTags = new ArrayList<>();
    private final String providerModId;
    private final LongSupplier ordinals;

    TooltipOutput(String providerModId, LongSupplier ordinals) {
        this.providerModId = providerModId;
        this.ordinals = ordinals;
    }

    /// Add one line of content.
    public Taggable add(Component component) {
        children.add(new TooltipEntry(component, childMetadata()));
        return new Taggable(this, children.size() - 1);
    }

    /// Add a multi-line block as a real {@link TooltipGroup}.
    public Taggable group(Consumer<TooltipOutput> consumer) {
        TooltipOutput child = new TooltipOutput(providerModId, ordinals);
        consumer.accept(child);
        children.add(new TooltipGroup(child.children, childMetadata()));
        return new Taggable(this, children.size() - 1);
    }

    /// Shorthand for {@code group(consumer).tag(tag)}: tag the block as a whole, not every line.
    public Taggable group(TooltipTag<?, ?> tag, Consumer<TooltipOutput> consumer) {
        return group(consumer).tag(tag);
    }

    /// Attach a plain tag to the source group built by {@link #toSourceGroup()} (used by the lifecycle bridge to
    /// stamp appender/component identity so an author's un-tagged output is still addressable).
    public TooltipOutput sourceTag(TooltipTag<?, ?> tag) {
        sourceTags.add(tag);
        return this;
    }

    private void tagAt(int index, TooltipTag<?, ?> tag) {
        children.set(index, TooltipNodes.withAddedTag(children.get(index), tag));
    }

    private TooltipMetadata childMetadata() {
        return TooltipMetadata.builder()
                .providerModId(providerModId)
                .declarationOrdinal(ordinals.getAsLong())
                .build();
    }

    TooltipGroup toSourceGroup() {
        var metadata = childMetadata().toBuilder();
        for (TooltipTag<?, ?> tag : sourceTags) {
            metadata.addTag(tag);
        }
        return new TooltipGroup(children, metadata.build());
    }

    /// Fluent handle returned by {@link #add}/{@link #group} for attaching a tag to the just-added node.
    public static final class Taggable {
        private final TooltipOutput output;
        private final int index;

        private Taggable(TooltipOutput output, int index) {
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
