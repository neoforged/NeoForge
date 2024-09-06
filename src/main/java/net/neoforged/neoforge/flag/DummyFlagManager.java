/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import java.util.Set;

final class DummyFlagManager implements FlagManager {
    private final Set<Flag> enabledFlags;

    public DummyFlagManager(Set<Flag> enabledFlags) {
        this.enabledFlags = Set.copyOf(enabledFlags);
    }

    @Override
    public boolean set(Flag flag, boolean state) {
        return false;
    }

    @Override
    public Set<Flag> getEnabledFlags() {
        return enabledFlags;
    }
}
