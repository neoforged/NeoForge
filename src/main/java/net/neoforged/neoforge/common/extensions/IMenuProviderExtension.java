/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Extension type for the {@link net.minecraft.world.MenuProvider} interface.
 */
public interface IMenuProviderExtension {
    /**
     * {@return {@code true} if the existing container should be closed on the client side when opening a new one, {@code false} otherwise}
     * 
     * @implNote Returning false prevents the mouse from being (re-)centered when opening a new container.
     */
    default boolean shouldTriggerClientSideContainerClosingOnOpen() {
        return true;
    }

    /**
     * Allows the menu provider to write additional data to be read by {@link net.neoforged.neoforge.network.IContainerFactory#create(int, Inventory, RegistryFriendlyByteBuf)}
     * when the menu is created on the client-side.
     *
     * @param menu   A server-side menu created by this menu provider.
     * @param buffer Additional data that will be sent to the client.
     */
    default void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {}
}
