/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.CustomCloudsRenderer;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom {@link CustomCloudsRenderer}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterCustomCloudsRendererEvent extends Event implements IModBusEvent {
    private final Map<Identifier, CustomCloudsRenderer> renderers;

    @ApiStatus.Internal
    public RegisterCustomCloudsRendererEvent(Map<Identifier, CustomCloudsRenderer> renderers) {
        this.renderers = renderers;
    }

    /**
     * Registers the renderer for a given custom clouds type.
     *
     * @see net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes#CUSTOM_CLOUDS
     */
    public void register(Identifier id, CustomCloudsRenderer effects) {
        if (NeoForgeEnvironmentAttributes.NO_CUSTOM_CLOUDS.equals(id)) {
            throw new IllegalArgumentException("You cannot register a renderer for the default clouds");
        }
        this.renderers.put(id, effects);
    }
}
