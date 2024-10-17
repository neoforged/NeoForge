/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.buffer.IBufferDefinition;
import net.neoforged.neoforge.client.buffer.chunk.ChunkBufferDefinitionManager;
import net.neoforged.neoforge.client.buffer.chunk.IChunkBufferCallback;
import net.neoforged.neoforge.client.buffer.chunk.ISectionLayerRenderer;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event fires various times after {@link RegisterBufferDefinitionsEvent} for registering {@link RenderType#chunkBufferLayers() vanilla chunk buffer layers } in different {@link RenderLevelStageEvent.Stage stages}
 * Check {@link #getStage()} to register in specific stages for your use case.
 * It is safe to build {@link net.neoforged.neoforge.client.buffer.IBufferDefinition} here or after the event is fired.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterChunkBufferDefinitionsEvent extends Event implements IModBusEvent {
    private final RenderLevelStageEvent.Stage stage;

    public RegisterChunkBufferDefinitionsEvent(RenderLevelStageEvent.Stage stage) {
        this.stage = stage;
    }

    /**
     * Register an {@link IBufferDefinition} into {@link RenderType#chunkBufferLayers() vanilla chunk buffer layers } in a specific {@link RenderLevelStageEvent.Stage stage} with a {@link IChunkBufferCallback#passThrough() default callback}
     * 
     * @param definition the buffer definition to be registered
     * @see ChunkBufferDefinitionManager#register(RenderLevelStageEvent.Stage, IBufferDefinition)
     */
    public void register(IBufferDefinition definition) {
        ChunkBufferDefinitionManager.register(stage, definition);
    }

    /**
     * Register an {@link IBufferDefinition} into {@link RenderType#chunkBufferLayers() vanilla chunk buffer layers } in a specific {@link RenderLevelStageEvent.Stage stage} with a {@link IChunkBufferCallback customized callback} that provides an {@link ISectionLayerRenderer}.
     * 
     * @param definition the buffer definition to be registered
     * @param callback   THe callback behaviour when the {@link IBufferDefinition chunk buffer} is rendered in level
     * @see ChunkBufferDefinitionManager#register(RenderLevelStageEvent.Stage, IBufferDefinition, IChunkBufferCallback)
     */
    public void register(IBufferDefinition definition, IChunkBufferCallback callback) {
        ChunkBufferDefinitionManager.register(stage, definition, callback);
    }

    /**
     * @return the {@link RenderLevelStageEvent.Stage stage} that the buffer definitions will be registered to. Check this before registering to ensure
     *         that rendering happens at the appropriate time.
     */
    public RenderLevelStageEvent.Stage getStage() {
        return stage;
    }

    @ApiStatus.Internal
    public static void collectChunkBufferDefinitions() {
        RenderLevelStageEvent.Stage.RENDER_TYPE_STAGES.values().forEach(stage -> ModLoader.postEvent(new RegisterChunkBufferDefinitionsEvent(stage)));
    }
}
