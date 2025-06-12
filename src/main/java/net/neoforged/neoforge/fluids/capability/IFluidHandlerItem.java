/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;

/**
 * @deprecated Superseded by using {@link IItemContext} when getting the capability of a {@link IResourceHandler}
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public interface IFluidHandlerItem extends IFluidHandler {
    /**
     * Get the container currently acted on by this fluid handler.
     * The ItemStack may be different from its initial state, in the case of fluid containers that have different items
     * for their filled and empty states.
     * May be an empty item if the container was drained and is consumable.
     */
    ItemStack getContainer();
}
