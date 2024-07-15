/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public interface FlagElement {
    Set<ResourceLocation> requiredFlags();

    default boolean isEnabled() {
        return Flags.isEnabled(requiredFlags());
    }
}
