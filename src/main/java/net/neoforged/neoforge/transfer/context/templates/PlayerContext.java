/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.context.templates;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.items.wrappers.PlayerInventoryHandler;

public class PlayerContext implements IItemContext {
    protected final PlayerInventoryHandler handler;
    protected final int index;

    public static IItemContext ofHand(Player player, InteractionHand hand) {
        if (player.getAbilities().instabuild) {
            ItemStack itemInHand = player.getItemInHand(hand);
            return new CreativePlayerContext(ItemResource.of(itemInHand), itemInHand.getCount(), player);
        }
        return new PlayerContext(player, hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : player.getInventory().getContainerSize() - 1);
    }

    public static IItemContext ofArmor(Player player, EquipmentSlot slot) {
        if (player.getAbilities().instabuild) {
            ItemStack itemInSlot = player.getItemBySlot(slot);
            return new CreativePlayerContext(ItemResource.of(itemInSlot), itemInSlot.getCount(), player);
        }
        return new PlayerContext(player, player.getInventory().getContainerSize() + slot.getIndex());
    }

    public PlayerContext(Player player, int index) {
        this.handler = new PlayerInventoryHandler(player);
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
    public int insert(ItemResource resource, int amount, TransferAction action) {
        if (action.isSimulating()) return amount;
        int inserted = handler.insert(index, resource, amount, action);
        if (inserted < amount && action.isExecuting()) {
            handler.insertOrDrop(resource, amount);
        }
        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return handler.extract(index, resource, amount, action);
    }

    @Override
    public int exchange(ItemResource resource, int amount, TransferAction action) {
        int currentAmount = getAmount();
        if (amount >= currentAmount) {
            if (action.isExecuting()) {
                handler.set(index, resource, currentAmount);
            }
            return currentAmount;
        }
        int extracted = extract(getResource(), amount, action);
        if (extracted > 0 && action.isExecuting()) {
            handler.insertOrDrop(resource, extracted);
        }
        return extracted;
    }
}
