/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to register {@link PipelineModifiers} to modify {@link RenderPipeline}s in arbitrary render paths.
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public final class RegisterPipelineModifiersEvent extends Event implements IModBusEvent {
    RegisterPipelineModifiersEvent() {}

    /**
     * Register a {@link PipelineModifier}.
     *
     * @param key      The key to register the modifier with, must be kept around for applying the modifier
     * @param modifier The modifier to register
     */
    public void register(ResourceKey<PipelineModifier> key, PipelineModifier modifier) {
        if (PipelineModifiers.MODIFIERS.putIfAbsent(key, modifier) != null) {
            throw new IllegalStateException("Duplicate modifier registration for key " + key);
        }
    }
}
