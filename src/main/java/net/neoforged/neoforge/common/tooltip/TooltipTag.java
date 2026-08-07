/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.Objects;
import net.minecraft.resources.Identifier;

/// A concrete tag attached to a {@link TooltipNode}: a {@link TooltipTagType} plus a value.
///
/// Equality is {@code (type.id(), type.nodeType(), value)} &mdash; value identity, never object reference.
/// Two independently-constructed {@code loreLine(0)} tags are equal and match the same nodes.
///
/// @param <N> the node type this tag attaches to
/// @param <V> the value type carried by this tag (use {@link Void} for marker tags)
public record TooltipTag<N extends TooltipNode, V>(TooltipTagType<N, V> type, V value) {
	public TooltipTag(TooltipTagType<N, V> type, V value) {
		this.type = Objects.requireNonNull(type, "type");
		this.value = value;
	}

	public Identifier id() {
		return type.id();
	}

	/// Convenience for {@code type() instanceof TooltipTagType.Negotiated}.
	public boolean isNegotiated() {
		return type instanceof TooltipTagType.Negotiated<?, ?>;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TooltipTag<?, ?> other)) return false;
		return type.id().equals(other.type.id())
				&& type.nodeType().equals(other.type.nodeType())
				&& Objects.equals(value, other.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type.id(), type.nodeType(), value);
	}

	@Override
	public String toString() {
		return "Tag(" + type.id() + "=" + value + ")";
	}
}
