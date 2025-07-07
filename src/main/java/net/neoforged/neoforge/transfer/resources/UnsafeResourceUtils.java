/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * <strong>Avoid use of this utility.</strong> This is only intended to be used internally by Neo or in read only
 * scenarios to avoid allocation where you can ensure no data is written back to the stack.
 */
//Deprecated annotations are java's standard as well for "unsafe" uses
@Deprecated
public final class UnsafeResourceUtils {
    /**
     * <strong>Avoid use of this method.</strong>
     * <p>
     * You shouldn't be calling this yourself. This is intended for <b>readonly</b> use in the ResourceHandler wrapper implementations.
     */
    @Deprecated
    public static ItemStack innerStackOf(ItemResource resource) {
        return resource.innerStack;
    }

    /**
     * <strong>Avoid use of this method.</strong>
     * <p>
     * You shouldn't be calling this yourself. This is intended for <b>readonly</b> use in the ResourceHandler wrapper implementations.
     */
    @Deprecated
    public static FluidStack innerStackOf(FluidResource resource) {
        return resource.innerStack;
    }

    private UnsafeResourceUtils() {}
}
