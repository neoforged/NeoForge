/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.opengl;

import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.Set;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceProperties;

record ImmutableGlDeviceProperties(
        String backendName,
        String apiName,
        String apiVersionString,
        int apiVersionMajor,
        int apiVersionMinor,
        int knownGpuBufferUsageBits,
        int knownGpuTextureUsageBits,
        Set<DepthTestFunction> knownDepthTestFunctions,
        Set<DestFactor> knownDestFactors,
        Set<SourceFactor> knownSourceFactors,
        Set<ShaderType> knownShaderTypes,
        Set<UniformType> knownUniformTypes,
        Set<AddressMode> knownAddressModes,
        Set<TextureFormat> knownTextureFormats,
        boolean depthZeroToOne,
        int maximumUniformBindings,
        int maximumUBOBindings,
        int maximumTextureBindings,
        int minimumUBOAlignment,
        int maximumUBOSize,
        int maximumTexelBufferSize,
        int maximumVertexInputAttributes,
        int maximumVertexInputBuffers,
        int maximumVertexOutputLocations,
        int maximumImageArrayLayers,
        int maximumImageDimension1D,
        int maximumImageDimension2D,
        int maximumImageDimension3D,
        int maximumImageDimensionCube) implements GpuDeviceProperties {
    ImmutableGlDeviceProperties(GpuDeviceProperties properties) {
        this(
                properties.backendName(),
                properties.apiName(),
                properties.apiVersionString(),
                properties.apiVersionMajor(),
                properties.apiVersionMinor(),
                properties.knownGpuBufferUsageBits(),
                properties.knownGpuTextureUsageBits(),
                properties.knownDepthTestFunctions(),
                properties.knownDestFactors(),
                properties.knownSourceFactors(),
                properties.knownShaderTypes(),
                properties.knownUniformTypes(),
                properties.knownAddressModes(),
                properties.knownTextureFormats(),
                properties.depthZeroToOne(),
                properties.maximumUniformBindings(),
                properties.maximumUBOBindings(),
                properties.maximumTextureBindings(),
                properties.minimumUBOAlignment(),
                properties.maximumUBOSize(),
                properties.maximumTexelBufferSize(),
                properties.maximumVertexInputAttributes(),
                properties.maximumVertexInputBuffers(),
                properties.maximumVertexOutputLocations(),
                properties.maximumImageArrayLayers(),
                properties.maximumImageDimension1D(),
                properties.maximumImageDimension2D(),
                properties.maximumImageDimension3D(),
                properties.maximumImageDimensionCube());
    }
}
