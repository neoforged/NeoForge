/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * This extension of {@link MenuType.MenuSupplier} allows a mod to handle the extra data it sent to the client
 * when creating the client-side copy of a menu.
 */
public interface IContainerFactory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {
    /**
     * Constructs a menu instance on the client-side in response to a menu being opened on the server-side for a player.
     *
     * @param windowId The {@link AbstractContainerMenu#containerId} of the menu on the server-side.
     * @param inv      Player inventory of the player for whom the menu is being created.
     * @param data     Additional data written by the server when the menu was opened.
     *                 It contains any data written by {@link net.neoforged.neoforge.common.extensions.IMenuProviderExtension#writeClientSideData(AbstractContainerMenu, RegistryFriendlyByteBuf)},
     *                 followed by optional contextual data written by the {@code extraDataWriter} argument to
     *                 {@link net.neoforged.neoforge.common.extensions.IPlayerExtension#openMenu(MenuProvider, Consumer)}.
     * @return The menu instance to use on the client-side.
     */
    T create(int windowId, Inventory inv, RegistryFriendlyByteBuf data);

    @Override
    default T create(int p_create_1_, Inventory p_create_2_) {
        return create(p_create_1_, p_create_2_, null);
    }
}
