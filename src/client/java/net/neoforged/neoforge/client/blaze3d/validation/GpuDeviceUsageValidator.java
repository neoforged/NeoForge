/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.validation;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceProperties;

/**
 * Helper class for validations done by the ValidationGpuDevice layer
 */
public class GpuDeviceUsageValidator {
    private final GpuDeviceProperties properties;
    private final GpuDeviceFeatures enabledFeatures;

    GpuDeviceUsageValidator(ValidationGpuDevice device) {
        properties = device.enabledProperties();
        enabledFeatures = device.enabledFeatures();
    }

    void validateBufferUsage(int usage) {
        if ((usage & ~properties.knownGpuBufferUsageBits()) != 0) {
            throw new IllegalArgumentException(String.format("Unknown GpuBuffer usage bits provided, 0x%X requested, 0x%X known", usage, properties.knownGpuBufferUsageBits()));
        }
    }

    void validateTextureUsage(int usage) {
        if ((usage & ~properties.knownGpuTextureUsageBits()) != 0) {
            throw new IllegalArgumentException(String.format("Unknown GpuTexture usage bits provided, 0x%X requested, 0x%X known", usage, properties.knownGpuTextureUsageBits()));
        }
    }

    void validateTextureFormat(TextureFormat format) {
        if (!properties.knownTextureFormats().contains(format)) {
            throw new IllegalArgumentException(String.format("Unknown by device TextureFormat (%s) provided, known formats are %s", format.name(), properties.knownTextureFormats()));
        }
    }

    void validateAddressMode(AddressMode mode) {
        if (!properties.knownAddressModes().contains(mode)) {
            throw new IllegalArgumentException(String.format("Unknown by device AddressMode (%s) provided, known formats are %s", mode.name(), properties.knownAddressModes()));
        }
    }

    public void validatePipeline(RenderPipeline pipeline) {
        if (pipeline.getColorLogic() != LogicOp.NONE && !enabledFeatures.logicOp()) {
            throw new IllegalArgumentException(String.format("Cannot use LogicOp other than NONE without enabling 'logicOp' device feature, %s", pipeline.getLocation()));
        }

        for (RenderPipeline.UniformDescription uniform : pipeline.getUniforms()) {
            if (!properties.knownUniformTypes().contains(uniform.type())) {
                throw new IllegalArgumentException(String.format("Unknown by device UniformType (%s) provided for uniform %s, known types are %s", uniform.type().name(), uniform.name(), properties.knownUniformTypes()));
            }
        }
        if (!properties.knownDepthTestFunctions().contains(pipeline.getDepthTestFunction())) {
            throw new IllegalArgumentException(String.format("Unknown by device DepthTestFunction (%s) provided, known functions are %s", pipeline.getDepthTestFunction().name(), properties.knownDepthTestFunctions()));
        }
        final var blendFunctionOpt = pipeline.getBlendFunction();
        if (blendFunctionOpt.isPresent()) {
            final var blendFunc = blendFunctionOpt.get();
            if (!properties.knownSourceFactors().contains(blendFunc.sourceColor())) {
                throw new IllegalArgumentException(String.format("Unknown by device SourceFactor (%s) provided for sourceColor, known factors are %s", blendFunc.sourceColor().name(), properties.knownSourceFactors()));
            }
            if (!properties.knownSourceFactors().contains(blendFunc.sourceAlpha())) {
                throw new IllegalArgumentException(String.format("Unknown by device SourceFactor (%s) provided for sourceAlpha, known factors are %s", blendFunc.sourceAlpha().name(), properties.knownSourceFactors()));
            }
            if (!properties.knownDestFactors().contains(blendFunc.destColor())) {
                throw new IllegalArgumentException(String.format("Unknown by device DestFactor (%s) provided for destColor, known factors are %s", blendFunc.destColor().name(), properties.knownDestFactors()));
            }
            if (!properties.knownDestFactors().contains(blendFunc.destAlpha())) {
                throw new IllegalArgumentException(String.format("Unknown by device DestFactor (%s) provided for destAlpha, known factors are %s", blendFunc.destAlpha().name(), properties.knownDestFactors()));
            }
        }
    }
}
