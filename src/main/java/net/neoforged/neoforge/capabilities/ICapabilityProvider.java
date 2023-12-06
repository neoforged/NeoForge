/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ICapabilityProvider<O, C, T> {
    /**
     * Returns the capability, or {@code null} if not available.
     *
     * <p><b>For block entities only</b>: If a previously returned capability is not valid anymore, or if a new capability is available,
     * {@link Level#invalidateCapabilities(BlockPos)} MUST be called to notify the caches (see {@link IBlockCapabilityProvider#getCapability}).
     *
     * @param object  The object that might provide the capability.
     * @param context Extra context, capability-dependent.
     */
    @Nullable
    T getCapability(O object, C context);
}
