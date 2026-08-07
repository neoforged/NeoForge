/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

/// Fallback placement for an {@code add} whose anchor was not found. {@link #NONE} (default) means the intent is a
/// no-op when the target is missing &mdash; there is <strong>never</strong> an implicit tail-append. Use
/// {@link TooltipEdit#orElseAfter} for a value-carrying fallback relative to another tag.
public enum TooltipFallback {
    /// Drop the intent if the anchor is unresolved (default).
    NONE,
    /// Place at the tail of the tooltip.
    TAIL,
    /// Place at the head of the tooltip.
    HEAD
}
