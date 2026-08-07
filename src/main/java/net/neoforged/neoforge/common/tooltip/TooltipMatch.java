/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import net.minecraft.network.chat.Component;

/// Deep-API selector over a tag's matches, returned by {@link TooltipNegotiation#match}. Deliberately a separate
/// type so {@code first/last/all} cannot leak into the terse one-liner surface. Select a subset, then call
/// {@link #remove()} or {@link #replace(Component...)}. Default selection is exact (one match).
public final class TooltipMatch {
    private final TooltipNegotiation negotiation;
    private final TooltipTag<?, ?> tag;
    private TooltipIntent.Selector selector = TooltipIntent.Selector.EXACT;

    TooltipMatch(TooltipNegotiation negotiation, TooltipTag<?, ?> tag) {
        this.negotiation = negotiation;
        this.tag = tag;
    }

    public int count() {
        return negotiation.snapshot().count(tag);
    }

    public TooltipMatch first() {
        this.selector = TooltipIntent.Selector.FIRST;
        return this;
    }

    public TooltipMatch last() {
        this.selector = TooltipIntent.Selector.LAST;
        return this;
    }

    public TooltipMatch all() {
        this.selector = TooltipIntent.Selector.ALL;
        return this;
    }

    public void remove() {
        negotiation.remove(tag).selector(selector);
    }

    public void replace(Component... replacement) {
        negotiation.replace(tag, replacement).selector(selector);
    }
}
