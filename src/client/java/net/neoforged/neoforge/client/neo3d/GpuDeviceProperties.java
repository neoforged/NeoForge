/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d;

import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.Set;
import org.jetbrains.annotations.ApiStatus;

public interface GpuDeviceProperties {
    /**
     * Name of the backend implementation
     * Can be used to identify backends that further extend B3D
     */
    String backendName();

    /**
     * The API the backend is using
     * ex: "OpenGL" or "Vulkan"
     */
    String apiName();

    /**
     * The API version, may contain more or less than two identifiers
     */
    String apiVersionString();

    int apiVersionMajor();

    int apiVersionMinor();

    // TODO: better docs on these, both for how to use and how to implement (and what restrictions any recommendation(s) places on Neo)
    //       the "known" properties are expected (but not required) to match whatever the Neo version the backend was built against supports
    //       for the enums, using EnumSet.range means inserting in the middle will get automatically added at runtime, which may not be desireable
    //       will need to be careful with the implementation, updating the enums, or both because of usages like that
    //       these are also currently not validated at all, a backend could just report it doesnt support something that is assumed to be support
    //       validation for that should probably be added, also for minimum required values of everything else
    int knownGpuBufferUsageBits();

    int knownGpuTextureUsageBits();

    Set<DepthTestFunction> knownDepthTestFunctions();

    Set<DestFactor> knownDestFactors();

    Set<SourceFactor> knownSourceFactors();

    Set<ShaderType> knownShaderTypes();

    Set<UniformType> knownUniformTypes();

    Set<AddressMode> knownAddressModes();

    Set<TextureFormat> knownTextureFormats();

    /**
     * Some APIs require a 0-1 depth range, this may affect shaders that rely on specific depth ranges
     */
    boolean depthZeroToOne();

    /**
     * Total uniforms that can be used by a single program
     * The sum of the per-type maximums may exceed this value
     */
    int maximumUniformBindings();

    /**
     * min(GL_MAX_VERTEX_UNIFORM_BLOCKS, GL_MAX_FRAGMENT_UNIFORM_BLOCKS)
     */
    int maximumUBOBindings();

    /**
     * Maximum number of textures that can be bound to a single pipeline.
     * TexelBuffers also count against this limit.
     * min(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, GL_MAX_TEXTURE_IMAGE_UNITS)
     */
    int maximumTextureBindings();

    /**
     * GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT
     * GpuDevice#getUniformOffsetAlignment()
     * This value is known to be greater than 1 on many GPUs
     */
    int minimumUBOAlignment();

    /**
     * GL_MAX_UNIFORM_BLOCK_SIZE
     */
    int maximumUBOSize();

    /**
     * GL_MAX_TEXTURE_BUFFER_SIZE
     */
    int maximumTexelBufferSize();

    /**
     * GL_MAX_VERTEX_ATTRIBS
     */
    int maximumVertexInputAttributes();

    /**
     * GL_MAX_VERTEX_ATTRIB_BINDINGS
     */
    int maximumVertexInputBuffers();

    /**
     * GL_MAX_VERTEX_OUTPUT_COMPONENTS / 4
     */
    int maximumVertexOutputLocations();

    /**
     * GL_MAX_ARRAY_TEXTURE_LAYERS
     */
    int maximumImageArrayLayers();

    /**
     * GL_MAX_TEXTURE_SIZE
     */
    int maximumImageDimension1D();

    /**
     * GL_MAX_TEXTURE_SIZE
     */
    int maximumImageDimension2D();

    /**
     * GL_MAX_3D_TEXTURE_SIZE
     */
    int maximumImageDimension3D();

    /**
     * GL_MAX_CUBE_MAP_TEXTURE_SIZE
     */
    int maximumImageDimensionCube();

    record Immutable(
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
        /**
         * Immutable record should only be constructed from an instance of the interface
         * <br>
         * Additional elements may be added without being considered a breaking change
         *
         * @see Immutable#Immutable(GpuDeviceProperties)
         */
        @ApiStatus.Internal
        public Immutable {}

        public Immutable(GpuDeviceProperties properties) {
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
}
