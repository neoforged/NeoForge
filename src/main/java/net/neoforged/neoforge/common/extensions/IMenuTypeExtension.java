/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.BooleanSupplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

public interface IMenuTypeExtension<T> {
    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory, BooleanSupplier isDisabled) {
        return new MenuType<>(factory, isDisabled);
    }

    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return create(factory, () -> false);
    }

    T create(int windowId, Inventory playerInv, FriendlyByteBuf extraData);
}
