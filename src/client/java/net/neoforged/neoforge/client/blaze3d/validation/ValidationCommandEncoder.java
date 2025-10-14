/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * Validation wrapper around CommandEncoder
 * <br>
 * This is used to wrap the RenderPass
 */
public class ValidationCommandEncoder implements CommandEncoder {
    private final CommandEncoder realCommandEncoder;
    private final GpuDeviceUsageValidator validator;

    protected ValidationCommandEncoder(CommandEncoder realCommandEncoder, GpuDeviceUsageValidator validator) {
        this.realCommandEncoder = realCommandEncoder;
        this.validator = validator;
    }

    protected ValidationRenderPass wrapRenderPass(RenderPass renderPass, GpuDeviceUsageValidator validator) {
        return new ValidationRenderPass(renderPass, validator);
    }

    @Override
    public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTextureView, OptionalInt clearColor) {
        if (colorTextureView instanceof ValidationGpuTextureView validationColorTextureView) {
            // TODO 1.21.8: Can't require a validated wrapper since we initially forgot and that'd make it a breaking change
            colorTextureView = validationColorTextureView.getRealTextureView();
        }
        return wrapRenderPass(realCommandEncoder.createRenderPass(label, colorTextureView, clearColor), validator);
    }

    @Override
    public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTextureView, OptionalInt clearColor, @Nullable GpuTextureView depthTextureView, OptionalDouble clearDepth) {
        if (colorTextureView instanceof ValidationGpuTextureView validationColorTextureView) {
            // TODO 1.21.8: Can't require a validated wrapper since we initially forgot and that'd make it a breaking change
            colorTextureView = validationColorTextureView.getRealTextureView();
        }
        if (depthTextureView instanceof ValidationGpuTextureView validationDepthTextureView) {
            // TODO 1.21.8: Can't require a validated wrapper since we initially forgot and that'd make it a breaking change
            depthTextureView = validationDepthTextureView.getRealTextureView();
        }

        return wrapRenderPass(realCommandEncoder.createRenderPass(label, colorTextureView, clearColor, depthTextureView, clearDepth), validator);
    }

    @Override
    public void clearColorTexture(GpuTexture texture, int clearColor) {
        if (!(texture instanceof ValidationGpuTexture validationTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.clearColorTexture(validationTexture.getRealTexture(), clearColor);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth) {
        if (!(colorTexture instanceof ValidationGpuTexture validationColorTexture)) {
            throw new IllegalArgumentException();
        }
        if (!(depthTexture instanceof ValidationGpuTexture validationDepthTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.clearColorAndDepthTextures(validationColorTexture.getRealTexture(), clearColor, validationDepthTexture.getRealTexture(), clearDepth);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth, int scissorX, int scissorY, int scissorWidth, int scissorHeight) {
        if (!(colorTexture instanceof ValidationGpuTexture validationColorTexture)) {
            throw new IllegalArgumentException();
        }
        if (!(depthTexture instanceof ValidationGpuTexture validationDepthTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.clearColorAndDepthTextures(validationColorTexture.getRealTexture(), clearColor, validationDepthTexture.getRealTexture(), clearDepth, scissorX, scissorY, scissorWidth, scissorHeight);
    }

    @Override
    public void clearDepthTexture(GpuTexture depthTexture, double clearDepth) {
        if (!(depthTexture instanceof ValidationGpuTexture validationDepthTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.clearDepthTexture(validationDepthTexture.getRealTexture(), clearDepth);
    }

    @Override
    public void clearStencilTexture(GpuTexture stencilTexture, int clearValue) {
        if (!(stencilTexture instanceof ValidationGpuTexture validationStencilTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.clearStencilTexture(validationStencilTexture.getRealTexture(), clearValue);
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
        if (!(texture instanceof ValidationGpuTexture validationTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.writeToTexture(validationTexture.getRealTexture(), image);
    }

    @Override
    public void writeToTexture(GpuTexture texture, NativeImage image, int mipLevel, int layer, int dstX, int dstY, int width, int height, int srcX, int srcY) {
        if (!(texture instanceof ValidationGpuTexture validationTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.writeToTexture(validationTexture.getRealTexture(), image, mipLevel, layer, dstX, dstY, width, height, srcX, srcY);
    }

    @Override
    public void writeToTexture(GpuTexture texture, ByteBuffer imageData, NativeImage.Format imageFormat, int mipLevel, int layer, int x, int y, int width, int height) {
        if (!(texture instanceof ValidationGpuTexture validationTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.writeToTexture(validationTexture.getRealTexture(), imageData, imageFormat, mipLevel, layer, x, y, width, height);
    }

    @Override
    public void copyTextureToBuffer(GpuTexture texture, GpuBuffer buffer, int bufferOffset, Runnable callback, int mipLevel) {
        if (!(texture instanceof ValidationGpuTexture validationTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.copyTextureToBuffer(validationTexture.getRealTexture(), buffer, bufferOffset, callback, mipLevel);
    }

    @Override
    public void copyTextureToBuffer(GpuTexture texture, GpuBuffer buffer, int bufferOffset, Runnable callback, int mipLevel, int x, int y, int width, int height) {
        if (!(texture instanceof ValidationGpuTexture validationTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.copyTextureToBuffer(validationTexture.getRealTexture(), buffer, bufferOffset, callback, mipLevel, x, y, width, height);
    }

    @Override
    public void copyTextureToTexture(GpuTexture srcTexture, GpuTexture dstTexture, int mipLevel, int srcX, int srcY, int dstX, int dstY, int width, int height) {
        if (!(srcTexture instanceof ValidationGpuTexture validationSrcTexture)) {
            throw new IllegalArgumentException();
        }
        if (!(dstTexture instanceof ValidationGpuTexture validationDstTexture)) {
            throw new IllegalArgumentException();
        }
        realCommandEncoder.copyTextureToTexture(validationSrcTexture.getRealTexture(), validationDstTexture.getRealTexture(), mipLevel, srcX, srcY, dstX, dstY, width, height);
    }

    @Override
    public void presentTexture(GpuTextureView textureView) {
        if (textureView instanceof ValidationGpuTextureView validationTextureView) {
            // TODO 1.21.8: Can't require a validated wrapper since we initially forgot and that'd make it a breaking change
            textureView = validationTextureView.getRealTextureView();
        }
        realCommandEncoder.presentTexture(textureView);
    }

    @Override
    public GpuFence createFence() {
        return realCommandEncoder.createFence();
    }
}
