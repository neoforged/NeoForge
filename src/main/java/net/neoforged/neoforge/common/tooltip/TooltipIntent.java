/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// A declarative negotiation intent submitted by a listener and resolved by the {@code TooltipArbitrator} after all
/// listeners return. There is deliberately <strong>no keep intent</strong>: absence of an intent means keep.
///
/// Every intent carries an {@link TooltipIntentKey} used both for deterministic conflict resolution and for
/// diagnostics ({@link TooltipIntentKey#providerId()} is the mod that owns the submitting listener).
@ApiStatus.Internal
public sealed interface TooltipIntent {
    TooltipIntentKey key();

    /// Add new lines, optionally anchored relative to existing nodes. TooltipIntent-unit atomic: if an anchor fails to
    /// resolve and no fallback applies, the whole add is dropped.
    record Add(
            TooltipIntentKey key,
            List<Component> content,
            @Nullable TooltipTag<?, ?> after,
            @Nullable TooltipTag<?, ?> before,
            TooltipFallback fallback,
            @Nullable TooltipTag<?, ?> fallbackTag,
            boolean fallbackAfter) implements TooltipIntent {
    }

    /// Remove node(s) matching {@code target} per {@code selector}.
    record Remove(TooltipIntentKey key, TooltipTag<?, ?> target, Selector selector) implements TooltipIntent {
    }

    /// Replace node(s) matching {@code target} with {@code replacement} per {@code selector}.
    record Replace(TooltipIntentKey key, TooltipTag<?, ?> target, Selector selector, List<Component> replacement) implements TooltipIntent {
    }

    /// Vote for {@code provider} in negotiated channel {@code negotiatedTagId} (candidate resolution input).
    record Prefer(TooltipIntentKey key, Identifier negotiatedTagId, String provider) implements TooltipIntent {
    }

    /// How a {@code remove}/{@code replace} intent selects target node(s) from a tag's matches.
    enum Selector {
        /// Require exactly one match (default; 0 matches =&gt; noop, &gt;1 =&gt; diagnostic + drop).
        EXACT,
        /// Apply to every match ({@code removeAll} / {@code match(tag).all()}).
        ALL,
        /// The deep {@code match(tag).first()} selector.
        FIRST,
        /// The deep {@code match(tag).last()} selector.
        LAST
    }
}
