/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.google.errorprone.annotations.ForOverride;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.flag.FeatureElement;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;

/**
 * Interface describing an element whose state can be toggled with {@link Flag flags}.
 *
 * @apiNote All vanilla {@link FeatureElement FeatureElements} are pre patched to directly support the {@link Flag} system.
 */
public interface FlagElement {
    /**
     * @return Immutable {@link Set} containing all required {@link Flag Flags} for this element to be enabled.
     */
    @ForOverride
    Set<Flag> requiredFlags();

    /**
     * @return {@code true} if this {@link FlagElement element} should be enabled, {@code false} otherwise.
     */
    @ApiStatus.NonExtendable
    default boolean isEnabled() {
        // all flags are enabled during data gen
        if (DatagenModLoader.isRunningDataGen())
            return true;

        return activeFlagManager().isEnabled(requiredFlags());
    }

    private FlagManager activeFlagManager() {
        // this exists purely so that we can call #isEnabled
        // from within FeatureElement.isFeatureEnabled
        // which is required to correctly hook into the vanilla system
        // without requiring a ton of patches to invoke our isEnabled check along side vanillas
        // and mods also needing to invoke ours alongside vanillas
        var server = ServerLifecycleHooks.getCurrentServer();

        if (server != null)
            return server.getModdedFlagManager();
        if (FMLEnvironment.dist.isClient())
            return Minecraft.getInstance().getModdedFlagManager();

        return FlagManager.NULL;
    }
}
