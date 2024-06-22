/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.items.wrappers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.HandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.items.ItemResource;

public class RecipeWrapper implements Container {
    protected final IResourceHandlerModifiable<ItemResource> handler;

    public RecipeWrapper(IResourceHandlerModifiable<ItemResource> handler) {
        this.handler = handler;
    }

    @Override
    public int getContainerSize() {
        return handler.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < handler.size(); i++) {
            if (!HandlerUtil.isIndexEmpty(handler, i)) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return handler.getResource(index).toStack(handler.getAmount(index));
    }

    @Override
    public ItemStack removeItem(int index, int amount) {
        if (HandlerUtil.isIndexEmpty(handler, index)) return ItemStack.EMPTY;
        ItemResource resource = handler.getResource(index);
        return resource.toStack(handler.extract(index, resource, amount, TransferAction.EXECUTE));
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        return removeItem(p_18951_, Integer.MAX_VALUE);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        handler.set(index, ItemResource.of(stack), stack.getCount());
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player p_18946_) {
        return false;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < handler.size(); i++) {
            handler.set(i, ItemResource.BLANK, 0);
        }
    }
}
