/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired when debug entries are registered.
 * <p>
 * This event is fired during the {@link DebugScreenEntries} initialization to allow registration of custom entries.
 * <p>
 * This event is fired on the mod event bus.
 */
public final class RegisterDebugEntriesEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, DebugScreenEntry> entries;

    @ApiStatus.Internal
    public RegisterDebugEntriesEvent(Map<ResourceLocation, DebugScreenEntry> entries) {
        this.entries = entries;
    }

    public void register(ResourceLocation id, DebugScreenEntry entry) {
        if (entries.putIfAbsent(id, entry) != null)
            throw new IllegalStateException("Duplicate DebugScreenEntry registration: " + id);
    }
}
