/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

/**
 * Extension type for the {@link net.minecraft.world.MenuProvider} interface.
 */
public interface IMenuProviderExtension {

    /**
     * {@return true if the existing container should be closed on the client side when opening a new one, false otherwise}
     * 
     * @implNote Returning false prevents the mouse from being (re-)centered when opening a new container.
     */
    default boolean shouldTriggerClientSideContainerClosingOnOpen() {
        return true;
    }
}
