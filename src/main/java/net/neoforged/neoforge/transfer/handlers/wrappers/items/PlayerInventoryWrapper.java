/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.EmptyHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.CombinedResourceWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.HandlerIndexWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.RangedHandlerWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class PlayerInventoryWrapper extends CombinedResourceWrapper<ItemResource> {
    public final IResourceHandler<ItemResource> invHandler;
    public final IResourceHandler<ItemResource> armorHandler;
    public final IResourceHandler<ItemResource> offHandHandler;
    public final IResourceHandler<ItemResource> mainHandHandler;

    public PlayerInventoryWrapper(Player player) {
        super(EmptyHandler.ITEM, ofInv(player), ofArmor(player), ofHand(player, InteractionHand.OFF_HAND));
        invHandler = handlers[0];
        armorHandler = handlers[1];
        offHandHandler = handlers[2];
        mainHandHandler = ofHand(player, InteractionHand.MAIN_HAND);
    }

    public static IResourceHandlerModifiable<ItemResource> ofInv(Player player) {
        PlayerInventoryHandler handler = new PlayerInventoryHandler(player);
        return new RangedHandlerWrapper.Modifiable<>(handler, 0, player.getInventory().items.size());
    }

    public static IResourceHandlerModifiable<ItemResource> ofHand(Player player, InteractionHand hand) {
        PlayerInventoryHandler handler = new PlayerInventoryHandler(player);
        var inv = player.getInventory();
        return new HandlerIndexWrapper.Modifiable<>(handler, hand == InteractionHand.MAIN_HAND ? inv.selected : handler.size() - 1);
    }

    public static IResourceHandlerModifiable<ItemResource> ofArmor(Player player) {
        return new RangedHandlerWrapper.Modifiable<>(new PlayerInventoryHandler(player), player.getInventory().items.size(), player.getInventory().items.size() + player.getInventory().armor.size());
    }
}
