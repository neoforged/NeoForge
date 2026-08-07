/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

/// A node in the structured tooltip tree. Structure is expressed by this sealed hierarchy, not by tags:
/// a node is either a {@link TooltipEntry} (one line of content) or a {@link TooltipGroup} (ordered children).
///
/// Identity and semantics live in {@link #metadata()} (tags, provider). Node objects are compared by
/// identity for document positioning; matching for negotiation is done by tag.
public sealed interface TooltipNode permits TooltipEntry, TooltipGroup {
    TooltipMetadata metadata();
}
