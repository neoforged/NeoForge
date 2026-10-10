/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.IContainerFactory;
import org.jetbrains.annotations.ApiStatus;

/// Extension type for the [MenuProvider] interface.
public interface IMenuProviderExtension {
    /// {@return whether the existing container should be explicitly closed on the client side when opening a new one}
    /// Returning `false` prevents the mouse from being (re-)centered when opening a new container.
    default boolean shouldTriggerClientSideContainerClosingOnOpen() {
        return true;
    }

    /// Allows the menu provider to write additional data to be read by [IContainerFactory#create(int, Inventory, RegistryFriendlyByteBuf)]
    /// when the menu is created on the client-side.
    ///
    /// @param player The player opening the menu through this provider
    /// @param menu   A server-side menu created by this menu provider
    /// @param buffer Additional data that will be sent to the client
    default void writeClientSideData(ServerPlayer player, AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        this.writeClientSideData(menu, buffer);
    }

    /// Allows the menu provider to write additional data to be read by [IContainerFactory#create(int, Inventory, RegistryFriendlyByteBuf)]
    /// when the menu is created on the client-side.
    ///
    /// @param menu   A server-side menu created by this menu provider
    /// @param buffer Additional data that will be sent to the client
    @ApiStatus.OverrideOnly
    default void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {}
}
