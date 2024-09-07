/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;

final class ImmutableFlagManager implements FlagManager {
    private final ReferenceSet<Flag> enabledFlags;

    public ImmutableFlagManager(ReferenceSet<Flag> enabledFlags) {
        this.enabledFlags = ReferenceSets.unmodifiable(enabledFlags);
    }

    @Override
    public boolean set(Flag flag, boolean state) {
        return false;
    }

    @Override
    public ReferenceSet<Flag> getEnabledFlags() {
        return enabledFlags;
    }
}
