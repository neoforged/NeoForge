/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.List;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/// Fluent handle for a single pending intent, returned by {@link TooltipNegotiation#add} and friends. Configure
/// with {@code before/after/priority/orElse}; it auto-commits when the listener returns.
///
/// <b>TooltipIntent-unit atomicity</b>: if any ordering constraint on an {@code add} fails to resolve (0 matches with
/// no fallback, or &gt;1), the <em>entire</em> unit is dropped and a single diagnostic is emitted &mdash; no
/// partial application.
public final class TooltipEdit {
    enum Kind { ADD, REMOVE, REPLACE, PREFER }

    private final TooltipNegotiation negotiation;
    private final Kind kind;
    private final long ordinal;

    private List<Component> content = List.of();
    @Nullable
    private TooltipTag<?, ?> target;
    @Nullable
    private TooltipTag<?, ?> after;
    @Nullable
    private TooltipTag<?, ?> before;
    private TooltipFallback fallback = TooltipFallback.NONE;
    @Nullable
    private TooltipTag<?, ?> fallbackTag;
    private boolean fallbackAfter;
    private int priority;
    private TooltipIntent.Selector selector = TooltipIntent.Selector.EXACT;
    private TooltipTagType.@Nullable Negotiated<?, ?> preferTag;
    @Nullable
    private String preferProvider;

    TooltipEdit(TooltipNegotiation negotiation, Kind kind, long ordinal) {
        this.negotiation = negotiation;
        this.kind = kind;
        this.ordinal = ordinal;
    }

    public TooltipEdit before(TooltipTag<?, ?> tag) {
        this.before = tag;
        return this;
    }

    public TooltipEdit after(TooltipTag<?, ?> tag) {
        this.after = tag;
        return this;
    }

    public TooltipEdit priority(int priority) {
        this.priority = priority;
        return this;
    }

    public TooltipEdit orElse(TooltipFallback fallback) {
        this.fallback = fallback;
        return this;
    }

    public TooltipEdit orElseAfter(TooltipTag<?, ?> tag) {
        this.fallbackTag = tag;
        this.fallbackAfter = true;
        return this;
    }

    public TooltipEdit orElseBefore(TooltipTag<?, ?> tag) {
        this.fallbackTag = tag;
        this.fallbackAfter = false;
        return this;
    }

    TooltipEdit selector(TooltipIntent.Selector selector) {
        this.selector = selector;
        return this;
    }

    TooltipEdit content(Component... components) {
        this.content = List.of(components);
        return this;
    }

    TooltipEdit target(TooltipTag<?, ?> target) {
        this.target = target;
        return this;
    }

    TooltipEdit prefer(TooltipTagType.Negotiated<?, ?> tag, String provider) {
        this.preferTag = tag;
        this.preferProvider = provider;
        return this;
    }

    void commit() {
        if (target == null && (kind == Kind.REMOVE || kind == Kind.REPLACE)) {
            throw new IllegalStateException(kind + " edit without a target");
        }
        TooltipIntentKey key = new TooltipIntentKey(priority, negotiation.providerId(), ordinal);
        switch (kind) {
            case ADD -> negotiation.addIntent(new TooltipIntent.Add(key, content, after, before, fallback, fallbackTag, fallbackAfter));
            case REMOVE -> negotiation.addIntent(new TooltipIntent.Remove(key, target, selector));
            case REPLACE -> negotiation.addIntent(new TooltipIntent.Replace(key, target, selector, content));
            case PREFER -> {
                if (preferTag != null && preferProvider != null) {
                    negotiation.addIntent(new TooltipIntent.Prefer(key, preferTag.id(), preferProvider));
                }
            }
        }
    }
}
