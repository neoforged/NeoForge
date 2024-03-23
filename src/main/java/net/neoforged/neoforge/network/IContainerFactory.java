/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IContainerFactory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {
    T create(int windowId, Inventory inv, RegistryFriendlyByteBuf data);

    @Override
    default T create(int p_create_1_, Inventory p_create_2_) {
        return create(p_create_1_, p_create_2_, null);
    }
}
