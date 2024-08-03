/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import java.util.Set;

public interface FlagElement {
    Set<Flag> requiredFlags();

    default boolean isEnabled() {
        return FlagManager.lookup().map(mgr -> mgr.isEnabled(this)).orElse(true);
    }
}
