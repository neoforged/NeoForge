/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class ValidationCommandEncoder implements CommandEncoder {
    private final CommandEncoder realCommandEncoder;
    private final GpuDeviceUsageValidator validator;

    ValidationCommandEncoder(CommandEncoder realCommandEncoder, GpuDeviceUsageValidator validator) {
        this.realCommandEncoder = realCommandEncoder;
        this.validator = validator;
    }

    protected ValidationRenderPass wrapRenderPass(RenderPass renderPass, GpuDeviceUsageValidator validator) {
        return new ValidationRenderPass(renderPass, validator);
    }

    @Override
    public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTextureView, OptionalInt clearColor) {
        return wrapRenderPass(realCommandEncoder.createRenderPass(label, colorTextureView, clearColor), validator);
    }

    @Override
    public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTextureView, OptionalInt clearColor, @Nullable GpuTextureView depthTextureView, OptionalDouble clearDepth) {
        return wrapRenderPass(realCommandEncoder.createRenderPass(label, colorTextureView, clearColor, depthTextureView, clearDepth), validator);
    }

    @Override
    public void clearColorTexture(GpuTexture texture, int clearColor) {
        assert texture instanceof ValidationGpuTexture;
        realCommandEncoder.clearColorTexture(((ValidationGpuTexture) texture).getRealTexture(), clearColor);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth) {
        assert colorTexture instanceof ValidationGpuTexture;
        assert depthTexture instanceof ValidationGpuTexture;
        realCommandEncoder.clearColorAndDepthTextures(((ValidationGpuTexture) colorTexture).getRealTexture(), clearColor, ((ValidationGpuTexture) depthTexture).getRealTexture(), clearDepth);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth, int scissorX, int scissorY, int scissorWidth, int scissorHeight) {
        assert colorTexture instanceof ValidationGpuTexture;
        assert depthTexture instanceof ValidationGpuTexture;
        realCommandEncoder.clearColorAndDepthTextures(((ValidationGpuTexture) colorTexture).getRealTexture(), clearColor, ((ValidationGpuTexture) depthTexture).getRealTexture(), clearDepth, scissorX, scissorY, scissorWidth, scissorHeight);
    }

    @Override
    public void clearDepthTexture(GpuTexture depthTexture, double clearDepth) {
        assert depthTexture instanceof ValidationGpuTexture;
        realCommandEncoder.clearDepthTexture(((ValidationGpuTexture) depthTexture).getRealTexture(), clearDepth);
    }

    @Override
    public void clearStencilTexture(GpuTexture stencilTexture, int clearValue) {
        assert stencilTexture instanceof ValidationGpuTexture;
        realCommandEncoder.clearStencilTexture(((ValidationGpuTexture) stencilTexture).getRealTexture(), clearValue);
    }

    @Override
    public void writeToBuffer(GpuBufferSlice bufferSlice, ByteBuffer data) {
        realCommandEncoder.writeToBuffer(bufferSlice, data);
    }

    @Override
    public GpuBuffer.MappedView mapBuffer(GpuBuffer buffer, boolean read, boolean write) {
        return realCommandEncoder.mapBuffer(buffer, read, write);
    }

    @Override
    public GpuBuffer.MappedView mapBuffer(GpuBufferSlice bufferSlice, boolean read, boolean write) {
        return realCommandEncoder.mapBuffer(bufferSlice, read, write);
    }

    @Override
    public void copyToBuffer(GpuBufferSlice src, GpuBufferSlice dst) {
        realCommandEncoder.copyToBuffer(src, dst);
    }

    @Override
    public void writeToTexture(GpuTexture texture, NativeImage image) {
        assert texture instanceof ValidationGpuTexture;
        realCommandEncoder.writeToTexture(((ValidationGpuTexture) texture).getRealTexture(), image);
    }

    @Override
    public void writeToTexture(GpuTexture texture, NativeImage image, int mipLevel, int layer, int dstX, int dstY, int width, int height, int srcX, int srcY) {
        assert texture instanceof ValidationGpuTexture;
        realCommandEncoder.writeToTexture(((ValidationGpuTexture) texture).getRealTexture(), image, mipLevel, layer, dstX, dstY, width, height, srcX, srcY);
    }

    @Override
    public void writeToTexture(GpuTexture texture, IntBuffer imageData, NativeImage.Format imageFormat, int mipLevel, int layer, int x, int y, int width, int height) {
        assert texture instanceof ValidationGpuTexture;
        realCommandEncoder.writeToTexture(((ValidationGpuTexture) texture).getRealTexture(), imageData, imageFormat, mipLevel, layer, x, y, width, height);
    }

    @Override
    public void copyTextureToBuffer(GpuTexture texture, GpuBuffer buffer, int bufferOffset, Runnable callback, int mipLevel) {
        assert texture instanceof ValidationGpuTexture;
        realCommandEncoder.copyTextureToBuffer(((ValidationGpuTexture) texture).getRealTexture(), buffer, bufferOffset, callback, mipLevel);
    }

    @Override
    public void copyTextureToBuffer(GpuTexture texture, GpuBuffer buffer, int bufferOffset, Runnable callback, int mipLevel, int x, int y, int width, int height) {
        assert texture instanceof ValidationGpuTexture;
        realCommandEncoder.copyTextureToBuffer(((ValidationGpuTexture) texture).getRealTexture(), buffer, bufferOffset, callback, mipLevel, x, y, width, height);
    }

    @Override
    public void copyTextureToTexture(GpuTexture srcTexture, GpuTexture dstTexture, int mipLevel, int srcX, int srcY, int dstX, int dstY, int width, int height) {
        assert srcTexture instanceof ValidationGpuTexture;
        assert dstTexture instanceof ValidationGpuTexture;
        realCommandEncoder.copyTextureToTexture(((ValidationGpuTexture) srcTexture).getRealTexture(), ((ValidationGpuTexture) dstTexture).getRealTexture(), mipLevel, srcX, srcY, dstX, dstY, width, height);
    }

    @Override
    public void presentTexture(GpuTextureView texture) {
        realCommandEncoder.presentTexture(texture);
    }

    @Override
    public GpuFence createFence() {
        return realCommandEncoder.createFence();
    }
}
