/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.buffer.IBufferDefinition;
import net.neoforged.neoforge.client.buffer.chunk.ChunkLayerBufferDefinitions;
import net.neoforged.neoforge.client.buffer.chunk.IChunkBufferCallback;
import net.neoforged.neoforge.client.buffer.chunk.ISectionLayerRenderer;

/**
 * This event fires various times after {@link RegisterBufferDefinitionEvent} for registering {@link RenderType#chunkBufferLayers() vanilla chunk buffer layers } in different {@link RenderLevelStageEvent.Stage stages}
 * Check {@link #getStage()} to register in specific stages for your use case.
 * It is safe to build {@link net.neoforged.neoforge.client.buffer.IBufferDefinition} here or after the event is fired.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterChunkBufferDefinitionEvent extends Event implements IModBusEvent {
    private final RenderLevelStageEvent.Stage stage;

    public RegisterChunkBufferDefinitionEvent(RenderLevelStageEvent.Stage stage) {
        this.stage = stage;
    }

    /**
     * Register an {@link IBufferDefinition} into {@link RenderType#chunkBufferLayers() vanilla chunk buffer layers } in a specific {@link RenderLevelStageEvent.Stage stage} with a {@link IChunkBufferCallback#passThrough() default callback}
     * 
     * @param definition the buffer definition to be registered
     * @see ChunkLayerBufferDefinitions#register(RenderLevelStageEvent.Stage, IBufferDefinition)
     */
    public void register(IBufferDefinition definition) {
        ChunkLayerBufferDefinitions.register(stage, definition);
    }

    /**
     * Register an {@link IBufferDefinition} into {@link RenderType#chunkBufferLayers() vanilla chunk buffer layers } in a specific {@link RenderLevelStageEvent.Stage stage} with a {@link IChunkBufferCallback customized callback} that provides an {@link ISectionLayerRenderer}.
     * 
     * @param definition the buffer definition to be registered
     * @param callback   THe callback behaviour when the {@link IBufferDefinition chunk buffer} is rendered in level
     * @see ChunkLayerBufferDefinitions#register(RenderLevelStageEvent.Stage, IBufferDefinition, IChunkBufferCallback)
     */
    public void register(IBufferDefinition definition, IChunkBufferCallback callback) {
        ChunkLayerBufferDefinitions.register(stage, definition, callback);
    }

    public RenderLevelStageEvent.Stage getStage() {
        return stage;
    }
}
