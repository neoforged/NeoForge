/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;

final class EmptyFlagManager implements FlagManager {
    @Override
    public boolean set(Flag flag, boolean state) {
        return false;
    }

    @Override
    public ReferenceSet<Flag> getEnabledFlags() {
        return ReferenceSets.emptySet();
    }
}
