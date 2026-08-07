/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.Comparator;
import org.jetbrains.annotations.ApiStatus;

/// Determinism / tie-break key for an intent. The {@link #PREFERENCE} comparator defines the winner when multiple
/// intents target the same negotiation unit; it is a function of {@code (priority, providerId, declarationOrdinal)}
/// only &mdash; never of listener fire order (distinct providers have distinct {@code providerId}s; same-provider
/// intents are disambiguated by a monotonic per-render {@code declarationOrdinal}).
///
/// {@code PREFERENCE.compare(a, b) < 0} means {@code a} is preferred (wins).
@ApiStatus.Internal
public record TooltipIntentKey(int priority, String providerId, long declarationOrdinal) {
    public static final Comparator<TooltipIntentKey> PREFERENCE = (a, b) -> {
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
