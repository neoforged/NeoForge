/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.ItemUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.wrappers.CombinedResourceHandlerWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.IndexedResourceHandlerWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.RangedResourceHandlerWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class PlayerInventoryHandler extends CombinedResourceHandlerWrapper.Modifiable<ItemResource>  {
    protected final Player player;

    public final IResourceHandlerModifiable<ItemResource> invHandler;
    public final IResourceHandlerModifiable<ItemResource> armorHandler;
    public final IResourceHandlerModifiable<ItemResource> offHandHandler;
    public final IResourceHandlerModifiable<ItemResource> mainHandHandler;

    public PlayerInventoryHandler(Player player) {
        super(ofInv(player), ofArmor(player), ofHand(player, InteractionHand.OFF_HAND));
        this.player = player;
        this.invHandler = (IResourceHandlerModifiable<ItemResource>) handlers[0];
        this.armorHandler = (IResourceHandlerModifiable<ItemResource>) handlers[1];
        this.offHandHandler = (IResourceHandlerModifiable<ItemResource>) handlers[2];
        this.mainHandHandler = ofHand(player, InteractionHand.MAIN_HAND);
    }

    public static IResourceHandlerModifiable<ItemResource> ofInv(Player player) {
        PlayerInventoryWrapper handler = new PlayerInventoryWrapper(player);
        return new RangedResourceHandlerWrapper.Modifiable<>(handler, 0, player.getInventory().getNonEquipmentItems().size());
    }

    public static IResourceHandlerModifiable<ItemResource> ofHand(Player player, InteractionHand hand) {
        PlayerInventoryWrapper handler = new PlayerInventoryWrapper(player);
        Inventory inv = player.getInventory();

        return new IndexedResourceHandlerWrapper.Modifiable<>(handler, hand == InteractionHand.MAIN_HAND ? inv.getSelectedSlot() : handler.size() - 1);
    }

    public static IResourceHandlerModifiable<ItemResource> ofArmor(Player player) {
        return new RangedResourceHandlerWrapper.Modifiable<>(new PlayerInventoryWrapper(player), player.getInventory().getNonEquipmentItems().size(), player.getInventory().getContainerSize());
    }

    public void insertOrDrop(ItemResource resource, int amount) {
        int inserted = insert(resource, amount, TransferAction.EXECUTE);
        if (inserted >= amount) return;

        ItemUtil.dropFromPlayer(player, resource, amount - inserted);
    }


    public static class AutoDrop extends PlayerInventoryHandler {
        public AutoDrop(Player player) {
            super(player);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransferAction action) {
            if (resource.isEmpty() || amount <= 0) return 0;
            if (action.isSimulating()) return amount;
            insertOrDrop(resource, amount);
            return amount;
        }
    }
}
