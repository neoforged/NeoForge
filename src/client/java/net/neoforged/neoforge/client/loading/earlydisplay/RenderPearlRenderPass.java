/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.IndexType;
import net.neoforged.fml.earlydisplay.render.backend.ELSBuffer;
import net.neoforged.fml.earlydisplay.render.backend.ELSBufferSlice;
import net.neoforged.fml.earlydisplay.render.backend.ELSRenderPass;
import net.neoforged.fml.earlydisplay.render.backend.ELSRenderPipeline;
import net.neoforged.fml.earlydisplay.render.backend.ELSTexture;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
final class RenderPearlRenderPass implements ELSRenderPass {
    private final RenderPearlRenderBackend backend;
    private final RenderPass renderPass;

    RenderPearlRenderPass(RenderPearlRenderBackend backend, RenderPass renderPass) {
        this.backend = backend;
        this.renderPass = renderPass;
    }

    @Override
    public void setViewport(int x, int y, int width, int height) {
        this.renderPass.setViewport(x, y, width, height);
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        this.renderPass.enableScissor(x, y, width, height);
    }

    @Override
    public void disableScissor() {
        this.renderPass.disableScissor();
    }

    @Override
    public void bindPipeline(ELSRenderPipeline pipeline) {
        this.renderPass.setPipeline(this.backend.getPipeline(pipeline));
    }

    @Override
    public void bindTexture(String name, @Nullable ELSTexture texture) {
        if (texture != null) {
            RenderPearlTexture b3dTexture = (RenderPearlTexture) texture;
            this.renderPass.setUniform(name, b3dTexture.view(), b3dTexture.sampler);
        } else {
            this.renderPass.setUniform(name, null, null);
        }
    }

    @Override
    public void bindUniform(String name, ELSBuffer buffer) {
        this.renderPass.setUniform(name, ((RenderPearlBuffer) buffer).unwrap());
    }

    @Override
    public void bindVertexBuffer(ELSBufferSlice buffer) {
        this.renderPass.setVertexBuffer(0, ((RenderPearlBufferSlice) buffer).unwrap());
    }

    @Override
    public void bindIndexBuffer(@Nullable ELSBuffer buffer) {
        if (buffer != null) {
            this.renderPass.setIndexBuffer(((RenderPearlBuffer) buffer).unwrap(), IndexType.SHORT);
        }
    }

    @Override
    public void draw(int firstVertex, int vertexCount) {
        this.renderPass.draw(vertexCount, 1, firstVertex, 0);
    }

    @Override
    public void drawIndexed(int firstVertex, int indexCount) {
        this.renderPass.drawIndexed(indexCount, 1, 0, firstVertex, 0);
    }

    @Override
    public void close() {
        this.renderPass.close();
    }
}
