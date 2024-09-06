/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import java.util.Collections;
import java.util.Set;

final class NullFlagManager implements FlagManager {
    @Override
    public boolean set(Flag flag, boolean state) {
        return false;
    }

    @Override
    public Set<Flag> getEnabledFlags() {
        return Collections.emptySet();
    }
}
