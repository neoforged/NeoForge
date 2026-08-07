/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.Objects;
import net.minecraft.network.chat.Component;

/// A leaf {@link TooltipNode}: one line of text plus {@link TooltipMetadata}.
/// {@link Component#empty()} is a real, addressable line.
public record TooltipEntry(Component component, TooltipMetadata metadata) implements TooltipNode {
	public TooltipEntry(Component component, TooltipMetadata metadata) {
		this.component = Objects.requireNonNull(component, "component");
		this.metadata = Objects.requireNonNull(metadata, "metadata");
	}

	@Override
	public String toString() {
		return "Entry[" + component.getString() + "]";
	}
}
