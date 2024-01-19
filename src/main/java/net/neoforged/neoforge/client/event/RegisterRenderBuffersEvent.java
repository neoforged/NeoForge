/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.SortedMap;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

public class RegisterRenderBuffersEvent extends Event implements IModBusEvent {
    private final SortedMap<RenderType, BufferBuilder> renderBuffers;

    @ApiStatus.Internal
    public RegisterRenderBuffersEvent(SortedMap<RenderType, BufferBuilder> renderBuffers) {
        this.renderBuffers = renderBuffers;
    }

    public void registerRenderBuffer(RenderType renderType) {
        registerRenderBuffer(renderType, new BufferBuilder(renderType.bufferSize()));
    }

    public void registerRenderBuffer(RenderType renderType, BufferBuilder renderBuffer) {
        if (renderBuffers.containsKey(renderType)) {
            throw new IllegalStateException("Duplicate attempt to register render buffer: " + renderType);
        }

        renderBuffers.put(renderType, renderBuffer);
    }
}
