/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.resource.VanillaServerListeners;
import org.jetbrains.annotations.ApiStatus;

/**
 * The main ResourceManager is recreated on each reload, just after {@link ReloadableServerResources}'s creation.
 *
 * The event is fired on each reload and lets modders add their own ReloadListeners, for server-side resources.
 * The event is fired on the {@link NeoForge#EVENT_BUS}
 */
public class AddServerReloadListenersEvent extends SortedReloadListenerEvent {
    private final ReloadableServerResources serverResources;
    private final RegistryAccess registryAccess;

    @ApiStatus.Internal
    public AddServerReloadListenersEvent(ReloadableServerResources serverResources, RegistryAccess registryAccess) {
        super(serverResources.listeners(), AddServerReloadListenersEvent::lookupName);
        this.serverResources = serverResources;
        this.registryAccess = registryAccess;
    }

    /**
     * @return The {@link ReloadableServerResources} being reloaded.
     */
    public ReloadableServerResources getServerResources() {
        return serverResources;
    }

    /**
     * This context object holds data relevant to the current reload, such as staged tags.
     * 
     * @return The condition context for the currently active reload.
     */
    public ICondition.IContext getConditionContext() {
        return serverResources.getConditionContext();
    }

    /**
     * Provides access to the loaded registries associated with these server resources.
     * All built-in and dynamic registries are loaded and frozen by this point.
     * 
     * @return The RegistryAccess context for the currently active reload.
     */
    public RegistryAccess getRegistryAccess() {
        return registryAccess;
    }

    private static ResourceLocation lookupName(PreparableReloadListener listener) {
        ResourceLocation key = VanillaServerListeners.getNameForClass(listener.getClass());
        if (key == null) {
            if (listener.getClass().getPackageName().startsWith("net.minecraft")) {
                throw new IllegalArgumentException("A key for the reload listener " + listener + " was not provided in VanillaServerListeners!");
            } else {
                throw new IllegalArgumentException("A non-vanilla reload listener " + listener + " was added via mixin before the AddReloadListenerEvent! Mod-added listeners must go through the event.");
            }
        }
        return key;
    }
}
