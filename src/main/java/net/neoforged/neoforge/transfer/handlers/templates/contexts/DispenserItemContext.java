/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A context that represents a dispenser's inventory.
 * <p>
 * Note: This context does <strong>not</strong> modify the underlying dispenser inventory.
 * The intended usage is to wrap the stack provided in {@link DefaultDispenseItemBehavior#execute(BlockSource, ItemStack)}.
 * You can then use {@link #finalizeResult(BlockSource)} to return the result to the dispenser and handle overflow.
 */
public class DispenserItemContext implements IItemContext {
    protected ItemResource resource;
    protected int amount;
    protected final Object2IntMap<ItemResource> resources = new Object2IntOpenHashMap<>();

    public DispenserItemContext(ItemStack stack) {
        this.resource = ItemResource.of(stack);
        this.amount = stack.getCount();
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
        int inserted = 0;
        if (getResource().isEmpty()) {
            inserted = Math.min(amount, resource.getMaxStackSize());
            this.resource = resource;
            this.amount = inserted;
        } else if (getResource().equals(resource)) {
            inserted = Math.min(amount, resource.getMaxStackSize() - getAmount());
            this.amount += inserted;
        }
        int remainder = amount - inserted;
        if (remainder > 0) {
            resources.mergeInt(resource, remainder, Integer::sum);
        }
        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int extracted = Math.min(amount, getAmount());
        //snapshot is handled by the handler itself
        this.amount -= extracted;
        if (getAmount() == 0) {
            this.resource = ItemResource.EMPTY;
        }
        return extracted;
    }

    public ItemStack finalizeResult(BlockSource source) {
        ItemStack res = resource.toStack(amount);
        List<ItemStack> overflow = new ArrayList<>();
        for (Object2IntMap.Entry<ItemResource> entry : resources.object2IntEntrySet()) {
            ItemResource key = entry.getKey();
            var value = entry.getIntValue();
            for (ItemStack stack : key.toStacks(value)) {
                ItemStack notInserted = source.blockEntity().insertItem(stack);
                if (!notInserted.isEmpty()) {
                    overflow.add(notInserted);
                }
            }
        }
        if (!overflow.isEmpty()) {
            Direction direction = source.state().getValue(DispenserBlock.FACING);
            DefaultDispenseItemBehavior.playDefaultSound(source);
            DefaultDispenseItemBehavior.playDefaultAnimation(source, direction);
            Position position = DispenserBlock.getDispensePosition(source);
            for (ItemStack stack : overflow) {
                DefaultDispenseItemBehavior.spawnItem(source.level(), stack, 6, direction, position);
            }
        }
        resources.clear();
        return res;
    }
}
