/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import java.util.SequencedMap;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired to allow mods to register custom {@linkplain BufferBuilder render buffers}.
 * This allows to have dedicated {@linkplain BufferBuilder render buffer} for each {@linkplain RenderType render type}
 * that can filled and rendered in batch
 * This event is fired after the default Minecraft render buffers have been registered.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterRenderBuffersEvent extends Event implements IModBusEvent {
    private final SequencedMap<RenderType, ByteBufferBuilder> renderBuffers;

    @ApiStatus.Internal
    public RegisterRenderBuffersEvent(SequencedMap<RenderType, ByteBufferBuilder> renderBuffers) {
        this.renderBuffers = renderBuffers;
    }

    /**
     * Registers a default render buffer with buffer size specified in the render type.
     *
     * @param renderType a render type of the render buffer
     */
    public void registerRenderBuffer(RenderType renderType) {
        registerRenderBuffer(renderType, new ByteBufferBuilder(renderType.bufferSize()));
    }

    /**
     * Registers a render buffer for specified render type.
     *
     * @param renderType   a render type of the render buffer
     * @param renderBuffer a render buffer to register
     */
    public void registerRenderBuffer(RenderType renderType, ByteBufferBuilder renderBuffer) {
        if (renderBuffers.containsKey(renderType)) {
            throw new IllegalStateException("Duplicate attempt to register render buffer: " + renderType);
        }

        renderBuffers.put(renderType, renderBuffer);
    }
}
