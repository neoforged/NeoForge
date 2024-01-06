/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IMenuFactory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {
    T create(int windowId, Inventory inv, FriendlyByteBuf data);

    @Override
    default T create(int windowId, Inventory inventory) {
        return create(windowId, inventory, null);
    }
}
