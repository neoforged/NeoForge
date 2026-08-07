/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.List;

/// Mutable builder for a tooltip during appender execution. Top-level {@link TooltipGroup}s are held in
/// {@code #3132} appender order. Call {@link #freeze()} to obtain the immutable {@link TooltipSnapshot} all
/// negotiation listeners read.
public final class TooltipDocument {
    private final List<TooltipGroup> groups = new ArrayList<>();
    private long ordinalCounter;

    /// Open a structured output owned by {@code providerModId}, bound to a fresh source group.
    public TooltipOutput newOutput(String providerModId) {
        return new TooltipOutput(providerModId, this::nextOrdinal);
    }

    /// Finalize an output into a top-level source group.
    public void addSourceGroup(TooltipOutput output) {
        groups.add(output.toSourceGroup());
    }

    /// Add a pre-built top-level group.
    public void addGroup(TooltipGroup group) {
        groups.add(group);
    }

    public TooltipSnapshot freeze() {
        return new TooltipSnapshot(groups);
    }

    private long nextOrdinal() {
        return ordinalCounter++;
    }
}
