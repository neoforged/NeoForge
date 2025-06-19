/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.legacy;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;

@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public final class LegacyFluidItemHandler extends LegacyFluidHandler implements IFluidHandlerItem {
    IItemContext itemContext;

    public LegacyFluidItemHandler(IResourceHandler<FluidResource> handler, IItemContext context) {
        super(handler);
        this.itemContext = context;
    }

    @Override
    public ItemStack getContainer() {
        return itemContext.getResource().toStack(itemContext.getAmount());
    }
}
