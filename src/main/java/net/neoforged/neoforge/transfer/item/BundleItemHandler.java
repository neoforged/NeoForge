/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;

/**
 * A {@link ComponentItemAccessItemHandler} for {@link BundleContents}.
 */
public class BundleItemHandler extends ComponentItemAccessItemHandler<BundleContents> {
    public BundleItemHandler(ItemAccess itemAccess, DataComponentType<BundleContents> component, int size) {
        super(itemAccess, component, size, () -> BundleContents.EMPTY);
    }

    @Override
    protected ItemStack getStackFromContents(BundleContents contents, int slot) {
        return slot < contents.size() ? contents.getItemUnsafe(slot) : ItemStack.EMPTY;
    }

    @Override
    protected BundleContents updateContents(BundleContents contents, int index, ItemStack stack) {
        List<ItemStack> list = new ObjectArrayList<>();
        contents.items().forEach(list::add);
        list.set(index, stack);
        return new BundleContents(list);
    }
}
