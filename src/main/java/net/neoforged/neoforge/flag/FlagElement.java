/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureElement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Base interface for all flagged elements.
 * <p>
 * All vanilla {@linkplain FeatureElement}s have been patched to directly support this system.
 * <p>
 * Custom implementors must only implement {@linkplain #requiredFlags()}.
 */
public interface FlagElement {
    /**
     * Returns set of flags which must all be enabled in order for this element to be enabled.
     *
     * @return Set of required flags.
     */
    @Unmodifiable
    Set<ResourceLocation> requiredFlags();

    /**
     * @return {@code true} if all required flags are enabled, {@code false} otherwise.
     */
    @ApiStatus.NonExtendable
    default boolean isEnabled() {
        return Flags.isEnabled(requiredFlags());
    }
}
