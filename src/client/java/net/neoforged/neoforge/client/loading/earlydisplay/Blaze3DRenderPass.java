package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.systems.RenderPass;
import net.neoforged.fml.earlydisplay.render.backend.ELSBuffer;
import net.neoforged.fml.earlydisplay.render.backend.ELSBufferSlice;
import net.neoforged.fml.earlydisplay.render.backend.ELSRenderPass;
import net.neoforged.fml.earlydisplay.render.backend.ELSRenderPipeline;
import net.neoforged.fml.earlydisplay.render.backend.ELSTexture;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
final class Blaze3DRenderPass implements ELSRenderPass {
    private final Blaze3DRenderBackend backend;
    private final RenderPass renderPass;

    Blaze3DRenderPass(Blaze3DRenderBackend backend, RenderPass renderPass) {
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
            Blaze3DTexture b3dTexture = (Blaze3DTexture) texture;
            this.renderPass.bindTexture(name, b3dTexture.view(), b3dTexture.sampler);
        } else {
            this.renderPass.bindTexture(name, null, null);
        }
    }

    @Override
    public void bindUniform(String name, ELSBuffer buffer) {
        this.renderPass.setUniform(name, ((Blaze3DBuffer) buffer).unwrap());
    }

    @Override
    public void bindVertexBuffer(ELSBufferSlice buffer) {
        this.renderPass.setVertexBuffer(0, ((Blaze3DBufferSlice) buffer).unwrap());
    }

    @Override
    public void bindIndexBuffer(@Nullable ELSBuffer buffer) {
        if (buffer != null) {
            this.renderPass.setIndexBuffer(((Blaze3DBuffer) buffer).unwrap(), IndexType.SHORT);
        }
    }

    @Override
    public void draw(int vertexCount) {
        this.renderPass.draw(vertexCount, 1, 0, 0);
    }

    @Override
    public void drawIndexed(int indexCount) {
        this.renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
    }

    @Override
    public void close() {
        this.renderPass.close();
    }
}
