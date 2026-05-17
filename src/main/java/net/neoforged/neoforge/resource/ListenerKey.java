/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import com.google.common.collect.MapMaker;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/// Identifies a [PreparableReloadListener] retained by the [ReloadableServerResources]
public final class ListenerKey<T extends PreparableReloadListener> {
    private static final ConcurrentMap<Identifier, ListenerKey<?>> VALUES = new MapMaker().weakValues().makeMap();

    private final Identifier listenerId;

    @SuppressWarnings("unchecked")
    public static <T extends PreparableReloadListener> ListenerKey<T> create(Identifier listenerId) {
        return (ListenerKey<T>) VALUES.computeIfAbsent(listenerId, ListenerKey::new);
    }

    private ListenerKey(Identifier listenerId) {
        this.listenerId = listenerId;
    }

    public Identifier getListenerId() {
        return this.listenerId;
    }

    @Override
    public String toString() {
        return this.listenerId.toString();
    }
}
