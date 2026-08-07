/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.Comparator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// A declarative negotiation intent submitted by a listener and resolved by the {@code TooltipArbitrator} after all
/// listeners return. There is deliberately <strong>no keep intent</strong>: absence of an intent means keep.
///
/// Every intent carries a {@link Key} used both for deterministic conflict resolution and for diagnostics
/// ({@link Key#providerId()} is the mod that owns the submitting listener).
@ApiStatus.Internal
public sealed interface TooltipIntent {
    Key key();

    /// Add new lines, optionally anchored relative to existing nodes. Intent-unit atomic: if an anchor fails to
    /// resolve and no fallback applies, the whole add is dropped.
    record Add(
            Key key,
            List<Component> content,
            @Nullable TooltipTag<?, ?> after,
            @Nullable TooltipTag<?, ?> before,
            TooltipNegotiation.Fallback fallback,
            @Nullable TooltipTag<?, ?> fallbackTag,
            boolean fallbackAfter) implements TooltipIntent {}

    /// Remove node(s) matching {@code target} per {@code selector}.
    record Remove(Key key, TooltipTag<?, ?> target, Selector selector) implements TooltipIntent {}

    /// Replace node(s) matching {@code target} with {@code replacement} per {@code selector}.
    record Replace(Key key, TooltipTag<?, ?> target, Selector selector, List<Component> replacement) implements TooltipIntent {}

    /// Vote for {@code provider} in negotiated channel {@code negotiatedTagId} (candidate resolution input).
    record Prefer(Key key, Identifier negotiatedTagId, String provider) implements TooltipIntent {}

    enum Selector {
        EXACT,
        ALL,
        FIRST,
        LAST
    }

    /// Determinism / tie-break key for an intent. The {@link #PREFERENCE} comparator defines the winner when
    /// multiple intents target the same negotiation unit; it is a function of
    /// {@code (priority, providerId, declarationOrdinal)} only &mdash; never of listener fire order (distinct
    /// providers have distinct {@code providerId}s; same-provider intents are disambiguated by a monotonic
    /// per-render {@code declarationOrdinal}).
    ///
    /// {@code PREFERENCE.compare(a, b) < 0} means {@code a} is preferred (wins).
    record Key(int priority, String providerId, long declarationOrdinal) {
        public static final Comparator<Key> PREFERENCE = (a, b) -> {
            // higher priority wins
            int c = Integer.compare(b.priority(), a.priority());
            if (c != 0) return c;
            // tie: smaller providerId wins
            c = a.providerId().compareTo(b.providerId());
            if (c != 0) return c;
            // tie: smaller declarationOrdinal wins
            return Long.compare(a.declarationOrdinal(), b.declarationOrdinal());
        };
    }
}
