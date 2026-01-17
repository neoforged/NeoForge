/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is emitted for each registry when the default data components for entries in that registry have
 * been first bound or updated.
 * <p>This can occur on the server when datapacks are reloaded or when the client receives this data upon joining a world.
 */
public class DefaultComponentsUpdatedEvent extends Event {
    private final ResourceKey<? extends Registry<?>> registry;

    @ApiStatus.Internal
    public DefaultComponentsUpdatedEvent(ResourceKey<? extends Registry<?>> registry) {
        this.registry = registry;
    }

    /**
     * {@return the key of the registry whose default components have updated}
     */
    public ResourceKey<? extends Registry<?>> getRegistry() {
        return registry;
    }
}
