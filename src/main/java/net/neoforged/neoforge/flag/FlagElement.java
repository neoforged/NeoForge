/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.google.errorprone.annotations.ForOverride;
import java.util.Set;
import net.minecraft.world.flag.FeatureElement;
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
        return FlagManager.lookup().map(mgr -> mgr.isEnabled(this)).orElseGet(FlagManager::shouldBeEnabledDefault);
    }
}
