/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.List;
import java.util.Objects;

/// A {@link TooltipNode} with ordered children. Used both for the auto-created per-appender <em>source groups</em>
/// and for explicit multi-line blocks authored via {@code output.group(...)}.
public record TooltipGroup(List<TooltipNode> children, TooltipMetadata metadata) implements TooltipNode {
	public TooltipGroup(List<TooltipNode> children, TooltipMetadata metadata) {
		this.children = List.copyOf(children);
		this.metadata = Objects.requireNonNull(metadata, "metadata");
	}

	@Override
	public String toString() {
		return "Group" + children;
	}
}
