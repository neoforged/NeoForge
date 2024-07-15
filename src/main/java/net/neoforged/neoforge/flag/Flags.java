/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public interface Flags {
    static Set<ResourceLocation> getFlags() {
        return FlagManager.INSTANCE.enabledFlagsView.keySet();
    }

    static boolean isEnabled(ResourceLocation flag) {
        return FlagManager.INSTANCE.enabledFlagsView.getOrDefault(flag, false);
    }

    static boolean isEnabled(ResourceLocation... flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }

    static boolean isEnabled(Iterable<ResourceLocation> flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }
}
