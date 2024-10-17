/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.chunk;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.buffer.IBufferDefinition;
import net.neoforged.neoforge.client.event.RegisterBufferDefinitionParamTypeAliasesEvent;
import net.neoforged.neoforge.client.event.RegisterBufferDefinitionsEvent;
import net.neoforged.neoforge.client.event.RegisterChunkBufferDefinitionsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Stores registered {@link IBufferDefinition} for {@link RenderType#chunkBufferLayers() vanilla chunk buffer layers}.
 * 
 * @see RenderType#chunkBufferLayers()
 */
public class ChunkBufferDefinitionManager {
    private static final Map<RenderLevelStageEvent.Stage, HashSet<Entry>> CHUNK_BUFFER_DEFINITIONS = new HashMap<>();

    /**
     * Register an {@link IBufferDefinition} into {@link RenderType#chunkBufferLayers() vanilla chunk buffer layers } with a {@link IChunkBufferCallback#passThrough() default callback}.
     * 
     * @param stage      The stage of the chunk buffer definition
     * @param definition The chunk buffer definition to be registered
     */
    public static void register(RenderLevelStageEvent.Stage stage, IBufferDefinition definition) {
        CHUNK_BUFFER_DEFINITIONS.computeIfAbsent(stage, k -> new HashSet<>()).add(new Entry(definition, IChunkBufferCallback.passThrough()));
    }

    /**
     * Register an {@link IBufferDefinition} into {@link RenderType#chunkBufferLayers() vanilla chunk buffer layers } with a {@link IChunkBufferCallback customized callback } that provides an {@link ISectionLayerRenderer}.
     * 
     * @param stage      The stage of the chunk buffer definition
     * @param definition The chunk buffer definition to be registered
     * @param callback   THe callback behaviour when the {@link IBufferDefinition chunk buffer} is rendered in level
     */
    public static void register(RenderLevelStageEvent.Stage stage, IBufferDefinition definition, IChunkBufferCallback callback) {
        CHUNK_BUFFER_DEFINITIONS.computeIfAbsent(stage, k -> new HashSet<>()).add(new Entry(definition, callback));
    }

    @ApiStatus.Internal
    public static void init() {
        ModLoader.postEvent(new RegisterBufferDefinitionParamTypeAliasesEvent());
        ModLoader.postEvent(new RegisterBufferDefinitionsEvent());
        RegisterChunkBufferDefinitionsEvent.collectChunkBufferDefinitions();
    }

    @ApiStatus.Internal
    public static Set<Entry> getChunkBufferDefinitions(RenderLevelStageEvent.Stage stage) {
        return CHUNK_BUFFER_DEFINITIONS.computeIfAbsent(stage, k -> new HashSet<>());
    }

    @ApiStatus.Internal
    public static Set<IBufferDefinition> getChunkBufferDefinitions() {
        return CHUNK_BUFFER_DEFINITIONS.values().stream().flatMap(Collection::stream).map(Entry::bufferDefinition).collect(Collectors.toSet());
    }

    @ApiStatus.Internal
    public record Entry(IBufferDefinition bufferDefinition, IChunkBufferCallback callback) {

    }
}
