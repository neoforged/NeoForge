/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

public interface IMenuTypeExtension<T> {
    /**
     * Use this method to create a menu type that uses additional data sent by the server when it creates
     * the client-side instances of its menus.
     *
     * @see IMenuProviderExtension#writeClientSideData(AbstractContainerMenu, RegistryFriendlyByteBuf)
     * @see IPlayerExtension#openMenu(MenuProvider, Consumer)
     */
    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS);
    }

    T create(int windowId, Inventory playerInv, RegistryFriendlyByteBuf extraData);
}
