/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A static context that holds a fixed amount of a single item. Operations on this context will still perform as if the
 * item is mutable, but the amount or resource will never change.
 */
public class StaticItemContext implements IItemContext {
    private final ItemResource resource;
    private final int amount;

    public StaticItemContext(ItemResource resource, int amount) {
        this.resource = resource;
        this.amount = amount;
    }

    public StaticItemContext(ItemStack stack) {
        this(ItemResource.of(stack), stack.getCount());
    }

    @Override
    public ItemResource getResource() {
        return resource;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount) || !resource.equals(this.resource)) return 0;
        return Math.min(this.amount, amount);
    }

    //TODO validating this works as expected without overriding. This currently is making something like buckets when in creative full fill the tank.
//    @Override
//    public int exchange(ItemResource resource, int amount, TransactionContext context) {
//        return amount;
//    }
}
