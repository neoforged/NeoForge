/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/// Internal operations on the immutable node tree (tag attachment, deep traversal, flatten).
final class TooltipNodes {
    private TooltipNodes() {}

    static TooltipNode withAddedTag(TooltipNode node, TooltipTag<?, ?> tag) {
        TooltipMetadata metadata = node.metadata().toBuilder().addTag(tag).build();
        if (node instanceof TooltipEntry entry) {
            return new TooltipEntry(entry.component(), metadata);
        }
        return new TooltipGroup(((TooltipGroup) node).children(), metadata);
    }

    /// Pre-order DFS over the tree rooted at {@code node}, visiting every node (including groups).
    static void preOrder(TooltipNode node, Consumer<TooltipNode> sink) {
        sink.accept(node);
        if (node instanceof TooltipGroup group) {
            for (TooltipNode child : group.children()) {
                preOrder(child, sink);
            }
        }
    }

    /// Flatten to the list of leaf entries in document order.
    static List<TooltipEntry> flatten(List<TooltipGroup> groups) {
        List<TooltipEntry> out = new ArrayList<>();
        for (TooltipGroup group : groups) {
            preOrder(group, node -> {
                if (node instanceof TooltipEntry entry) {
                    out.add(entry);
                }
            });
        }
        return out;
    }
}
