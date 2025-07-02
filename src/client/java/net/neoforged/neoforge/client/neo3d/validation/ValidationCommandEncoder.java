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

    @Override
    public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTextureView, OptionalInt clearColor) {
        return new ValidationRenderPass(realCommandEncoder.createRenderPass(label, colorTextureView, clearColor), validator);
    }

    @Override
    public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTextureView, OptionalInt clearColor, @Nullable GpuTextureView depthTextureView, OptionalDouble clearDepth) {
        return new ValidationRenderPass(realCommandEncoder.createRenderPass(label, colorTextureView, clearColor, depthTextureView, clearDepth), validator);
    }

    @Override
    public void clearColorTexture(GpuTexture texture, int clearColor) {
        realCommandEncoder.clearColorTexture(texture, clearColor);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth) {
        realCommandEncoder.clearColorAndDepthTextures(colorTexture, clearColor, depthTexture, clearDepth);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth, int scissorX, int scissorY, int scissorWidth, int scissorHeight) {
        realCommandEncoder.clearColorAndDepthTextures(colorTexture, clearColor, depthTexture, clearDepth, scissorX, scissorY, scissorWidth, scissorHeight);
    }

    @Override
    public void clearDepthTexture(GpuTexture depthTexture, double clearDepth) {
        realCommandEncoder.clearDepthTexture(depthTexture, clearDepth);
    }

    @Override
    public void clearStencilTexture(GpuTexture stencilTexture, int clearValue) {
        realCommandEncoder.clearStencilTexture(stencilTexture, clearValue);
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
        realCommandEncoder.writeToTexture(texture, image);
    }

    @Override
    public void writeToTexture(GpuTexture texture, NativeImage image, int mipLevel, int layer, int dstX, int dstY, int width, int height, int srcX, int srcY) {
        realCommandEncoder.writeToTexture(texture, image, mipLevel, layer, dstX, dstY, width, height, srcX, srcY);
    }

    @Override
    public void writeToTexture(GpuTexture texture, IntBuffer imageData, NativeImage.Format imageFormat, int mipLevel, int layer, int x, int y, int width, int height) {
        realCommandEncoder.writeToTexture(texture, imageData, imageFormat, mipLevel, layer, x, y, width, height);
    }

    @Override
    public void copyTextureToBuffer(GpuTexture texture, GpuBuffer buffer, int bufferOffset, Runnable callback, int mipLevel) {
        realCommandEncoder.copyTextureToBuffer(texture, buffer, bufferOffset, callback, mipLevel);
    }

    @Override
    public void copyTextureToBuffer(GpuTexture texture, GpuBuffer buffer, int bufferOffset, Runnable callback, int mipLevel, int x, int y, int width, int height) {
        realCommandEncoder.copyTextureToBuffer(texture, buffer, bufferOffset, callback, mipLevel, x, y, width, height);
    }

    @Override
    public void copyTextureToTexture(GpuTexture srcTexture, GpuTexture dstTexture, int mipLevel, int srcX, int srcY, int dstX, int dstY, int width, int height) {
        realCommandEncoder.copyTextureToTexture(srcTexture, dstTexture, mipLevel, srcX, srcY, dstX, dstY, width, height);
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
