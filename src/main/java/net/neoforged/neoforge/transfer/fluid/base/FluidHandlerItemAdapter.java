/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid.base;

import com.google.common.primitives.Ints;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.fluid.FluidVariant;
import net.neoforged.neoforge.transfer.initem.InItemStorageContext;
import net.neoforged.neoforge.transfer.storage.Storage;

/**
 * Adapts a {@link Storage} to {@link IFluidHandlerItem} with auto-commit behavior.
 * Make sure to provide the same {@link InItemStorageContext} as what the storage uses.
 */
public class FluidHandlerItemAdapter extends FluidHandlerAdapter implements IFluidHandlerItem {
    private final InItemStorageContext context;

    public FluidHandlerItemAdapter(InItemStorageContext ctx, Storage<FluidVariant> storage) {
        super(storage);
        this.context = ctx;
    }

    @Override
    public ItemStack getContainer() {
        return context.getCurrent().toStack(Ints.saturatedCast(context.getCurrentAmount()));
    }
}
