/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.resource.ListenerKey;
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
    private final Map<ListenerKey<?>, PreparableReloadListener> retainedListeners;

    @ApiStatus.Internal
    public AddServerReloadListenersEvent(ReloadableServerResources serverResources, Map<ListenerKey<?>, PreparableReloadListener> retainedListeners) {
        super(serverResources.listeners(), AddServerReloadListenersEvent::lookupName);
        this.serverResources = serverResources;
        this.retainedListeners = retainedListeners;
    }

    /// Adds a new [`reload listener`][PreparableReloadListener] to the resource manager and retains it in the [ReloadableServerResources]
    /// for later access through [ReloadableServerResources#getListener(ListenerKey)()].
    ///
    /// Unless explicitly specified, this listener will run after all vanilla listeners, in the order it was registered.
    ///
    /// @param key      The resource location that identifies the reload listener for dependency sorting and lookup.
    /// @param listener The listener to add.
    ///
    /// @throws IllegalArgumentException if another listener with that key was already registered.
    public <T extends PreparableReloadListener> void addRetainedListener(ListenerKey<T> key, T listener) {
        this.addListener(key.getListenerId(), listener);
        this.retainedListeners.put(key, listener);
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

    private static Identifier lookupName(PreparableReloadListener listener) {
        Identifier key = VanillaServerListeners.getNameForClass(listener.getClass());
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
