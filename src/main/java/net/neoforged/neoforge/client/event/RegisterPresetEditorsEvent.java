/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

/**
 * <p>Event for registering {@link PresetEditor} screen factories for world presets.</p>
 *
 * <p>This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the {@linkplain FMLModContainer#getEventBus() mod-specific event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterPresetEditorsEvent extends Event implements IModBusEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<ResourceKey<WorldPreset>, PresetEditor> editors;

    @ApiStatus.Internal
    public RegisterPresetEditorsEvent(Map<ResourceKey<WorldPreset>, PresetEditor> editors) {
        this.editors = editors;
    }

    /**
     * Registers a PresetEditor for a given world preset key.
     */
    public void register(ResourceKey<WorldPreset> key, PresetEditor editor) {
        PresetEditor old = this.editors.put(key, editor);
        if (old != null) {
            LOGGER.debug("PresetEditor {} overridden by mod {}", key.location(), Thread.currentThread().getStackTrace()[2]);
        }
    }
}
