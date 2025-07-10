/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.opengl;

import static org.lwjgl.opengl.GL11C.glGetInteger;
import static org.lwjgl.opengl.GL30C.GL_MAJOR_VERSION;
import static org.lwjgl.opengl.GL30C.GL_MINOR_VERSION;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceProperties;

public class NeoGlDeviceProperties implements GpuDeviceProperties {
    @Override
    public String backendName() {
        return "Default";
    }

    @Override
    public String apiName() {
        return "OpenGL";
    }

    @Override
    public String apiVersionString() {
        return String.format("%d.%d", apiVersionMajor(), apiVersionMinor());
    }

    @Override
    public int apiVersionMajor() {
        return glGetInteger(GL_MAJOR_VERSION);
    }

    @Override
    public int apiVersionMinor() {
        return glGetInteger(GL_MINOR_VERSION);
    }

    @Override
    public int knownGpuBufferUsageBits() {
        return (GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER << 1) - 1;
    }

    @Override
    public int knownGpuTextureUsageBits() {
        return (GpuTexture.USAGE_CUBEMAP_COMPATIBLE << 1) - 1;
    }

    @Override
    public Set<DepthTestFunction> knownDepthTestFunctions() {
        return Collections.unmodifiableSet(EnumSet.range(DepthTestFunction.NO_DEPTH_TEST, DepthTestFunction.GREATER_DEPTH_TEST));
    }

    @Override
    public Set<DestFactor> knownDestFactors() {
        return Collections.unmodifiableSet(EnumSet.range(DestFactor.CONSTANT_ALPHA, DestFactor.ZERO));
    }

    @Override
    public Set<SourceFactor> knownSourceFactors() {
        return Collections.unmodifiableSet(EnumSet.range(SourceFactor.CONSTANT_ALPHA, SourceFactor.ZERO));
    }

    @Override
    public Set<ShaderType> knownShaderTypes() {
        return Collections.unmodifiableSet(EnumSet.range(ShaderType.VERTEX, ShaderType.FRAGMENT));
    }

    @Override
    public Set<UniformType> knownUniformTypes() {
        return Collections.unmodifiableSet(EnumSet.range(UniformType.UNIFORM_BUFFER, UniformType.TEXEL_BUFFER));
    }

    @Override
    public Set<AddressMode> knownAddressModes() {
        return Collections.unmodifiableSet(EnumSet.range(AddressMode.REPEAT, AddressMode.CLAMP_TO_EDGE));
    }

    @Override
    public Set<TextureFormat> knownTextureFormats() {
        return Collections.unmodifiableSet(EnumSet.range(TextureFormat.RGBA8, TextureFormat.DEPTH32_STENCIL8));
    }
}
