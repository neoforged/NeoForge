/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.context.templates;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.items.wrappers.PlayerInventoryHandler;

public class PlayerContext extends SimpleItemContext {
    protected final Player player;

    public static PlayerContext ofHand(Player player, InteractionHand hand) {
        return new PlayerContext(player, hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : player.getInventory().getContainerSize() - 1);
    }

    public static PlayerContext ofArmor(Player player, EquipmentSlot slot) {
        return new PlayerContext(player, player.getInventory().getContainerSize() + slot.getIndex());
    }

    public PlayerContext(Player player, int index) {
        super(new PlayerInventoryHandler(player), index);
        this.player = player;
    }
}
