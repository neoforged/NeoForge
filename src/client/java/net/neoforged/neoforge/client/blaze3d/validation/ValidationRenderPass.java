/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collection;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * Validation wrapper around RenderPass
 * <br>
 * Validates RenderPipieline usages.
 */
public class ValidationRenderPass implements RenderPass {
    private final RenderPass realRenderPass;
    private final GpuDeviceUsageValidator validator;

    protected ValidationRenderPass(RenderPass realRenderPass, GpuDeviceUsageValidator validator) {
        this.realRenderPass = realRenderPass;
        this.validator = validator;
    }

    @Override
    public void pushDebugGroup(Supplier<String> label) {
        realRenderPass.pushDebugGroup(label);
    }

    @Override
    public void popDebugGroup() {
        realRenderPass.popDebugGroup();
    }

    @Override
    public void setPipeline(RenderPipeline pipeline) {
        validator.validatePipeline(pipeline);
        realRenderPass.setPipeline(pipeline);
    }

    @Override
    public void bindSampler(String name, @Nullable GpuTextureView textureView) {
        if (textureView instanceof ValidationGpuTextureView validationTextureView) {
            // TODO 1.21.8: Can't require a validated wrapper since we initially forgot and that'd make it a breaking change
            textureView = validationTextureView.getRealTextureView();
        }
        realRenderPass.bindSampler(name, textureView);
    }

    @Override
    public void setUniform(String name, GpuBuffer buffer) {
        realRenderPass.setUniform(name, buffer);
    }

    @Override
    public void setUniform(String name, GpuBufferSlice bufferSlice) {
        realRenderPass.setUniform(name, bufferSlice);
    }

    @Override
    public void setViewport(int x, int y, int width, int height) {
        realRenderPass.setViewport(x, y, width, height);
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        realRenderPass.enableScissor(x, y, width, height);
    }

    @Override
    public void disableScissor() {
        realRenderPass.disableScissor();
    }

    @Override
    public void setVertexBuffer(int index, GpuBuffer vertexBuffer) {
        realRenderPass.setVertexBuffer(index, vertexBuffer);
    }

    @Override
    public void setIndexBuffer(GpuBuffer indexBuffer, VertexFormat.IndexType indexType) {
        realRenderPass.setIndexBuffer(indexBuffer, indexType);
    }

    @Override
    public void drawIndexed(int vertexOffset, int firstIndex, int indexCount, int instanceCount) {
        realRenderPass.drawIndexed(vertexOffset, firstIndex, indexCount, instanceCount);
    }

    @Override
    public <T> void drawMultipleIndexed(Collection<Draw<T>> draws, @Nullable GpuBuffer indexBuffer, @Nullable VertexFormat.IndexType indexType, Collection<String> dynamicUniforms, T userData) {
        realRenderPass.drawMultipleIndexed(draws, indexBuffer, indexType, dynamicUniforms, userData);
    }

    @Override
    public void draw(int firstVertex, int vertexCount) {
        realRenderPass.draw(firstVertex, vertexCount);
    }

    @Override
    public void close() {
        realRenderPass.close();
    }
}
