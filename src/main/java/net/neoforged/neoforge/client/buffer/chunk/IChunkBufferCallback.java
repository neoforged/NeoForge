/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.chunk;

import net.minecraft.client.renderer.LevelRenderer;
import net.neoforged.neoforge.client.buffer.IBufferDefinition;
import org.joml.Matrix4f;

/**
 * Controls the behaviour when the {@link IBufferDefinition chunk buffer} is rendered.
 */
@FunctionalInterface
public interface IChunkBufferCallback {
    /**
     * Fires when the {@link IBufferDefinition chunk buffer} is rendered in level.
     * 
     * @param sectionLayerRenderer THe modified render parameters acceptor that finally passed parameters to section layer rendering
     * @param levelRenderer        the level renderer
     * @param bufferDefinition     the chunk buffer definition to be rendered
     * @param x                    camera position x
     * @param y                    camera position y
     * @param z                    camera position z
     * @param modelViewMatrix      the model-view matrix
     * @param projectionMatrix     the projection matrix
     */
    void onRenderChunkBuffer(ISectionLayerRenderer sectionLayerRenderer, LevelRenderer levelRenderer, IBufferDefinition bufferDefinition, float x, float y, float z, Matrix4f modelViewMatrix, Matrix4f projectionMatrix);

    static IChunkBufferCallback passThrough() {
        return (sectionLayerRenderer, levelRenderer, bufferDefinition, x, y, z, modelViewMatrix, projectionMatrix) -> sectionLayerRenderer.renderSectionLayer(x, y, z, modelViewMatrix, projectionMatrix);
    }
}
