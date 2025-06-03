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
import net.neoforged.neoforge.transfer.handlers.wrappers.items.InventoryWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A context that represents a player's inventory slot.
 */
public class PlayerContext implements IItemContext {
    protected final InventoryWrapper handler;
    protected final int index;

    public static IItemContext ofHand(Player player, InteractionHand hand) {
        if (player.getAbilities().instabuild) {
            ItemStack itemInHand = player.getItemInHand(hand);
            return new CreativePlayerContext(ItemResource.of(itemInHand), itemInHand.getCount(), player);
        }
        return new PlayerContext(player, hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : player.getInventory().getContainerSize() - 1);
    }

    public static IItemContext ofArmor(Player player, EquipmentSlot slot) {
        if (player.isCreative()) {
            ItemStack itemInSlot = player.getItemBySlot(slot);
            return new CreativePlayerContext(ItemResource.of(itemInSlot), itemInSlot.getCount(), player);
        }
        return new PlayerContext(player, player.getInventory().getContainerSize() + slot.getIndex());
    }

    public PlayerContext(Player player, int index) {
        //This could be captured by player.getCapability, but it was pointed out that has a non-zero chance to return null
        this.handler = InventoryWrapper.of(player);
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
        int inserted = handler.insert(index, resource, amount, transaction);
        if (inserted < amount) {
            var size = handler.size();
            for (var handlerIndex = 0; handlerIndex < size; handlerIndex++) {
                if (index == handlerIndex) continue;
                inserted += handler.insert(handlerIndex, resource, amount - inserted, transaction);
            }
        }
        if (inserted < amount) {
            handler.drop(resource, amount - inserted, true, true, transaction);
        }
        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return handler.extract(index, resource, amount, transaction);
    }
}
