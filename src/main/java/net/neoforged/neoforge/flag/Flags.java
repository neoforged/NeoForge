/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public interface Flags {
    /**
     * @return Set of all known flags
     */
    static Set<ResourceLocation> getFlags() {
        return FlagManager.INSTANCE.enabledFlagsView.keySet();
    }

    /**
     * @param flag Flag to be validated
     * @return {@code true} if the given flag is enabled, {@code false} otherwise.
     */
    static boolean isEnabled(ResourceLocation flag) {
        return FlagManager.INSTANCE.enabledFlagsView.getOrDefault(flag, false);
    }

    /**
     * @param flags Array of flags to be validated
     * @return {@code true} if all provided flags are enabled, {@code false} otherwise.
     */
    static boolean isEnabled(ResourceLocation... flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }

    /**
     * @param flags Iterable of flags to be validated
     * @return {@code true} if all provided flags are enabled, {@code false} otherwise.
     */
    static boolean isEnabled(Iterable<ResourceLocation> flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }
}
