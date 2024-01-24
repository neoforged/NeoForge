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
     * {@return {@code true} if the existing container should be closed on the client side when opening a new one, {@code false} otherwise}
     * 
     * @implNote Returning false prevents the mouse from being (re-)centered when opening a new container.
     */
    default boolean shouldTriggerClientSideContainerClosingOnOpen() {
        return true;
    }
}
