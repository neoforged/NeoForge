/*
 * Copyright (c) NeoForged and contributors
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
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ImmutableGlDeviceProperties(
        String backendName,
        String apiName,
        int knownGpuBufferUsageBits,
        int knownGpuTextureUsageBits,
        Set<DepthTestFunction> knownDepthTestFunctions,
        Set<DestFactor> knownDestFactors,
        Set<SourceFactor> knownSourceFactors,
        Set<ShaderType> knownShaderTypes,
        Set<UniformType> knownUniformTypes,
        Set<AddressMode> knownAddressModes,
        Set<TextureFormat> knownTextureFormats) implements GpuDeviceProperties {
    public ImmutableGlDeviceProperties(GpuDeviceProperties properties) {
        this(
                properties.backendName(),
                properties.apiName(),
                properties.knownGpuBufferUsageBits(),
                properties.knownGpuTextureUsageBits(),
                properties.knownDepthTestFunctions(),
                properties.knownDestFactors(),
                properties.knownSourceFactors(),
                properties.knownShaderTypes(),
                properties.knownUniformTypes(),
                properties.knownAddressModes(),
                properties.knownTextureFormats());
    }
}
