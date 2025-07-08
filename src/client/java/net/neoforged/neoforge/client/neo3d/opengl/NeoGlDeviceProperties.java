/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d.opengl;

import static org.lwjgl.opengl.ARBVertexAttribBinding.GL_MAX_VERTEX_ATTRIB_BINDINGS;
import static org.lwjgl.opengl.GL11C.glGetInteger;
import static org.lwjgl.opengl.GL20C.GL_MAX_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.opengl.GL20C.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.opengl.GL30C.GL_MAJOR_VERSION;
import static org.lwjgl.opengl.GL30C.GL_MINOR_VERSION;
import static org.lwjgl.opengl.GL31C.GL_MAX_3D_TEXTURE_SIZE;
import static org.lwjgl.opengl.GL31C.GL_MAX_ARRAY_TEXTURE_LAYERS;
import static org.lwjgl.opengl.GL31C.GL_MAX_CUBE_MAP_TEXTURE_SIZE;
import static org.lwjgl.opengl.GL31C.GL_MAX_FRAGMENT_UNIFORM_BLOCKS;
import static org.lwjgl.opengl.GL31C.GL_MAX_TEXTURE_BUFFER_SIZE;
import static org.lwjgl.opengl.GL31C.GL_MAX_TEXTURE_SIZE;
import static org.lwjgl.opengl.GL31C.GL_MAX_UNIFORM_BLOCK_SIZE;
import static org.lwjgl.opengl.GL31C.GL_MAX_VERTEX_ATTRIBS;
import static org.lwjgl.opengl.GL31C.GL_MAX_VERTEX_UNIFORM_BLOCKS;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static org.lwjgl.opengl.GL32C.GL_MAX_VERTEX_OUTPUT_COMPONENTS;

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
import net.neoforged.neoforge.client.neo3d.GpuDeviceProperties;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

public class NeoGlDeviceProperties implements GpuDeviceProperties {
    private final GLCapabilities glCaps = GL.getCapabilities();

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

    @Override
    public boolean depthZeroToOne() {
        return false;
    }

    @Override
    public int maximumUniformBindings() {
        return maximumUBOBindings() + maximumTextureBindings();
    }

    @Override
    public int maximumUBOBindings() {
        return Math.min(glGetInteger(GL_MAX_VERTEX_UNIFORM_BLOCKS), glGetInteger(GL_MAX_FRAGMENT_UNIFORM_BLOCKS));
    }

    @Override
    public int maximumTextureBindings() {
        return Math.min(glGetInteger(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS), glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS));
    }

    @Override
    public int minimumUBOAlignment() {
        return glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
    }

    @Override
    public int maximumUBOSize() {
        return glGetInteger(GL_MAX_UNIFORM_BLOCK_SIZE);
    }

    @Override
    public int maximumTexelBufferSize() {
        return glGetInteger(GL_MAX_TEXTURE_BUFFER_SIZE);
    }

    @Override
    public int maximumVertexInputAttributes() {
        return glGetInteger(GL_MAX_VERTEX_ATTRIBS);
    }

    @Override
    public int maximumVertexInputBuffers() {
        if (glCaps.GL_ARB_vertex_attrib_binding) {
            return glGetInteger(GL_MAX_VERTEX_ATTRIB_BINDINGS);
        } else {
            // older GL doesn't specify, but as many attribs is a valid limit in that case
            return maximumVertexInputAttributes();
        }
    }

    @Override
    public int maximumVertexOutputLocations() {
        return glGetInteger(GL_MAX_VERTEX_OUTPUT_COMPONENTS) / 4;
    }

    @Override
    public int maximumImageArrayLayers() {
        return glGetInteger(GL_MAX_ARRAY_TEXTURE_LAYERS);
    }

    @Override
    public int maximumImageDimension1D() {
        return glGetInteger(GL_MAX_TEXTURE_SIZE);
    }

    @Override
    public int maximumImageDimension2D() {
        return maximumImageDimension1D();
    }

    @Override
    public int maximumImageDimension3D() {
        return glGetInteger(GL_MAX_3D_TEXTURE_SIZE);
    }

    @Override
    public int maximumImageDimensionCube() {
        return glGetInteger(GL_MAX_CUBE_MAP_TEXTURE_SIZE);
    }
}
