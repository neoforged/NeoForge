/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.items.wrappers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.wrappers.HandlerIndexWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.RangedHandlerWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.ScopedHandlerWrapper;
import net.neoforged.neoforge.transfer.items.ItemResource;
import org.jetbrains.annotations.Nullable;

public class PlayerInventoryHandler extends ContainerWrapper {
    protected final Player player;

    public static IResourceHandlerModifiable<ItemResource> ofHands(Player player) {
        PlayerInventoryHandler handler = new PlayerInventoryHandler(player);
        return new ScopedHandlerWrapper.Modifiable<>(handler, new int[]{player.getInventory().selected, handler.size()});
    }

    public static IResourceHandlerModifiable<ItemResource> ofHand(Player player, InteractionHand hand) {
        PlayerInventoryHandler handler = new PlayerInventoryHandler(player);
        return new HandlerIndexWrapper.Modifiable<>(handler, hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : handler.size() - 1);
    }

    public static IResourceHandlerModifiable<ItemResource> ofArmor(Player player) {
        return new RangedHandlerWrapper.Modifiable<>(new PlayerInventoryHandler(player), player.getInventory().items.size(), player.getInventory().items.size() + player.getInventory().armor.size());
    }

    public PlayerInventoryHandler(Player player) {
        super(player.getInventory());
        this.player = player;
    }

    @Override
    public int getLimit(int index, ItemResource resource) {
        return getEquipmentSlot(index) != null ? 1 : super.getLimit(index, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        EquipmentSlot slot = getEquipmentSlot(index);
        return slot != null ? resource.canEquip(slot, player) : super.isValid(index, resource);
    }

    @Nullable
    protected EquipmentSlot getEquipmentSlot(int slot) {
        Inventory inv = player.getInventory();
        if (slot < inv.items.size()) {
            return null;
        }
        int equipmentSlot = slot - inv.items.size();
        if (equipmentSlot < inv.armor.size()) {
            return EquipmentSlot.values()[equipmentSlot + 2];
        }
        return null;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        EquipmentSlot slot = getEquipmentSlot(index);
        if (slot != null && !resource.canUnequip()) return 0;
        return super.extract(index, resource, amount, action);
    }

    public void drop(ItemResource resource, int amount) {
        resource.toStacks(amount).forEach(stack -> player.drop(stack, false));
    }

    public void insertOrDrop(ItemResource resource, int amount) {
        int inserted = insert(resource, amount, TransferAction.EXECUTE);
        if (inserted < amount) {
            drop(resource, amount - inserted);
        }
    }
}
