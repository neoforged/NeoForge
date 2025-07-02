/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d.validation;

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
import net.neoforged.neoforge.client.neo3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.neo3d.GpuDeviceProperties;
import org.jetbrains.annotations.Nullable;

public class ValidationGpuDevice implements GpuDevice {
    protected final GpuDevice realDevice;
    protected final GpuDeviceUsageValidator validator;
    private final ValidationCommandEncoder validationCommandEncoder;

    public ValidationGpuDevice(GpuDevice realDevice) {
        this.realDevice = realDevice;
        validator = new GpuDeviceUsageValidator(this);
        validationCommandEncoder = wrapCommandEncoder(realDevice.createCommandEncoder(), validator);
    }

    public GpuDevice getRealDevice() {
        return realDevice;
    }

    protected ValidationCommandEncoder wrapCommandEncoder(CommandEncoder commandEncoder, GpuDeviceUsageValidator validator){
        return new ValidationCommandEncoder(commandEncoder, validator);
    }
    
    @Override
    public CommandEncoder createCommandEncoder() {
        return validationCommandEncoder;
    }

    @Override
    public GpuTexture createTexture(@Nullable Supplier<String> label, int usage, TextureFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        validator.validateTextureUsage(usage);
        validator.validateTextureSize(usage, width, height, depthOrLayers);
        return realDevice.createTexture(label, usage, format, width, height, depthOrLayers, mipLevels);
    }

    @Override
    public GpuTexture createTexture(@Nullable String label, int usage, TextureFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        validator.validateTextureUsage(usage);
        validator.validateTextureSize(usage, width, height, depthOrLayers);
        return realDevice.createTexture(label, usage, format, width, height, depthOrLayers, mipLevels);
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture texture) {
        return realDevice.createTextureView(texture);
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
        return realDevice.createTextureView(texture, baseMipLevel, mipLevels);
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
    public GpuDeviceProperties properties() {
        return realDevice.properties();
    }

    @Override
    public GpuDeviceFeatures enabledFeatures() {
        return realDevice.enabledFeatures();
    }
}
