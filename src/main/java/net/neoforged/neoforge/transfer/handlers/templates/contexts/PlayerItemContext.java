/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A context that represents a player's inventory slot.
 */
public class PlayerItemContext implements IItemContext {
    protected final PlayerInventoryWrapper handler;
    protected final int index;

    public static IItemContext ofHand(Player player, InteractionHand hand) {
        if (player.getAbilities().instabuild) {
            ItemStack itemInHand = player.getItemInHand(hand);
            return new CreativePlayerItemContext(ItemResource.of(itemInHand), itemInHand.getCount(), player);
        }
        return new PlayerItemContext(player, hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : player.getInventory().getContainerSize() - 1);
    }

    public static IItemContext ofEquipmentSlot(Player player, EquipmentSlot slot) {
        if (player.getAbilities().instabuild) {
            ItemStack itemInSlot = player.getItemBySlot(slot);
            return new CreativePlayerItemContext(ItemResource.of(itemInSlot), itemInSlot.getCount(), player);
        }
        return new PlayerItemContext(player, player.getInventory().getContainerSize() + slot.getIndex());
    }

    public PlayerItemContext(Player player, int index) {
        this.handler = PlayerInventoryWrapper.of(player);
        this.index = index;
    }

    @Override
    public ItemResource getResource() {
        return handler.getResource(index);
    }

    @Override
    public int getAmount() {
        return handler.getAmount(index);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        //Try inserting to the specified index of the context first
        int inserted = handler.insert(index, resource, amount, transaction);
        //If we still have some items, try filling the rest of the inventory with the remaining.
        if (amount > inserted) {
            handler.placeItemBackInInventory(resource, amount - inserted, transaction);
        }
        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return handler.extract(index, resource, amount, transaction);
    }
}
