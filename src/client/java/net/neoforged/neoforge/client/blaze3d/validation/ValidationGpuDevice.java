/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceProperties;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * The validation GpuDevice is used to ensure that both mods and backends are complying with the B3D API contract correctly.
 * <p>
 * Validation done by this class includes:
 * <ul>
 * <li>Known texture/buffer usage bits</li>
 * <li>Known NonExhaustiveEnum values</li>
 * <li>Validates RenderPipeline precompilation</li>
 * </ul>
 */
public class ValidationGpuDevice implements GpuDevice {
    protected final GpuDevice realDevice;
    protected final GpuDeviceUsageValidator validator;
    private final ValidationCommandEncoder validationCommandEncoder;

    public ValidationGpuDevice(GpuDevice realDevice, boolean checkReservedUsageBits) {
        this.realDevice = realDevice;
        validator = new GpuDeviceUsageValidator(this, checkReservedUsageBits);
        validationCommandEncoder = wrapCommandEncoder(realDevice.createCommandEncoder(), validator);
    }

    @ApiStatus.Internal
    public GpuDevice getRealDevice() {
        return realDevice;
    }

    protected ValidationCommandEncoder wrapCommandEncoder(CommandEncoder commandEncoder, GpuDeviceUsageValidator validator) {
        return new ValidationCommandEncoder(commandEncoder, validator);
    }

    @Override
    public CommandEncoder createCommandEncoder() {
        return validationCommandEncoder;
    }

    protected ValidationGpuTexture wrapGpuTexture(GpuTexture texture, GpuDeviceUsageValidator validator) {
        return new ValidationGpuTexture(texture, validator);
    }

    @Override
    public GpuTexture createTexture(@Nullable Supplier<String> label, int usage, TextureFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        validator.validateTextureUsage(usage);
        return wrapGpuTexture(realDevice.createTexture(label, usage, format, width, height, depthOrLayers, mipLevels), validator);
    }

    @Override
    public GpuTexture createTexture(@Nullable String label, int usage, TextureFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        validator.validateTextureUsage(usage);
        return wrapGpuTexture(realDevice.createTexture(label, usage, format, width, height, depthOrLayers, mipLevels), validator);
    }

    protected ValidationGpuTextureView wrapGpuTextureView(ValidationGpuTexture validationGpuTexture, GpuTextureView gpuTextureView, GpuDeviceUsageValidator validator) {
        return new ValidationGpuTextureView(validationGpuTexture, gpuTextureView, validator);
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture texture) {
        if (!(texture instanceof ValidationGpuTexture validationTexture)) {
            throw new IllegalArgumentException();
        }
        return wrapGpuTextureView(validationTexture, realDevice.createTextureView(validationTexture.getRealTexture()), validator);
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
        if (!(texture instanceof ValidationGpuTexture validationTexture)) {
            throw new IllegalArgumentException();
        }
        return wrapGpuTextureView(validationTexture, realDevice.createTextureView(validationTexture.getRealTexture(), baseMipLevel, mipLevels), validator);
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> label, int usage, int size) {
        validator.validateBufferUsage(usage);
        return realDevice.createBuffer(label, usage, size);
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> label, int usage, ByteBuffer data) {
        validator.validateBufferUsage(usage);
        return realDevice.createBuffer(label, usage, data);
    }

    @Override
    public String getImplementationInformation() {
        return realDevice.getImplementationInformation();
    }

    @Override
    public List<String> getLastDebugMessages() {
        return realDevice.getLastDebugMessages();
    }

    @Override
    public boolean isDebuggingEnabled() {
        return realDevice.isDebuggingEnabled();
    }

    @Override
    public String getVendor() {
        return realDevice.getVendor();
    }

    @Override
    public String getBackendName() {
        return realDevice.getBackendName();
    }

    @Override
    public String getVersion() {
        return realDevice.getVersion();
    }

    @Override
    public String getRenderer() {
        return realDevice.getRenderer();
    }

    @Override
    public int getMaxTextureSize() {
        return realDevice.getMaxTextureSize();
    }

    @Override
    public int getUniformOffsetAlignment() {
        return realDevice.getUniformOffsetAlignment();
    }

    @Override
    public CompiledRenderPipeline precompilePipeline(RenderPipeline pipeline, @Nullable BiFunction<ResourceLocation, ShaderType, String> shaderSourceProvider) {
        validator.validatePipeline(pipeline);
        return realDevice.precompilePipeline(pipeline, shaderSourceProvider);
    }

    @Override
    public void clearPipelineCache() {
        realDevice.clearPipelineCache();
    }

    @Override
    public List<String> getEnabledExtensions() {
        return realDevice.getEnabledExtensions();
    }

    @Override
    public void close() {
        realDevice.close();
    }

    @Override
    public GpuDeviceProperties deviceProperties() {
        return realDevice.deviceProperties();
    }

    @Override
    public GpuDeviceFeatures enabledFeatures() {
        return realDevice.enabledFeatures();
    }
}
