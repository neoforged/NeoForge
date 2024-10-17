/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.chunk;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

@FunctionalInterface
public interface ISectionLayerRenderer {
    /**
     * Pass the modified render parameters to section layer rendering.
     * 
     * @param x                camera position x
     * @param y                camera position y
     * @param z                camera position z
     * @param modelViewMatrix  the model-view matrix
     * @param projectionMatrix the projection matrix
     */
    void renderSectionLayer(double x, double y, double z, Matrix4f modelViewMatrix, Matrix4f projectionMatrix);

    @ApiStatus.Internal
    static ISectionLayerRenderer vanilla(RenderType renderType, LevelRenderer levelRenderer) {
        return (x, y, z, modelViewMatrix, projectionMatrix) -> levelRenderer.renderSectionLayer(renderType, x, y, z, modelViewMatrix, projectionMatrix);
    }
}
