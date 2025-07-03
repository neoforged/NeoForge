/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d.validation;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.Arrays;
import net.neoforged.neoforge.client.neo3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.neo3d.GpuDeviceProperties;

public class GpuDeviceUsageValidator {
    private final GpuDeviceProperties properties;
    private final GpuDeviceFeatures enabledFeatures;

    GpuDeviceUsageValidator(ValidationGpuDevice device) {
        properties = device.properties();
        enabledFeatures = device.enabledFeatures();
    }

    void validateBufferUsage(int usage) {
        if ((usage & ~properties.knownGpuBufferUsageBits) != 0) {
            throw new IllegalArgumentException(String.format("Unknown GpuBuffer usage bits provided, 0x%X requested, 0x%X known", usage, properties.knownGpuBufferUsageBits));
        }
    }

    void validateTextureUsage(int usage) {
        if ((usage & ~properties.knownGpuTextureUsageBits) != 0) {
            throw new IllegalArgumentException(String.format("Unknown GpuTexture usage bits provided, 0x%X requested, 0x%X known", usage, properties.knownGpuTextureUsageBits));
        }
    }

    void validateTextureFormat(TextureFormat format) {
        if (!properties.knownTextureFormats.contains(format)) {
            throw new IllegalArgumentException(String.format("Unknown by device TextureFormat (%s) provided, known formats are %s", format.name(), Arrays.toString(properties.knownTextureFormats.toArray())));
        }
    }

    void validateTextureSize(int usage, int width, int height, int depthOrLayers) {
        if (height == 1 && depthOrLayers == 1) {
            // 1d texture
            if (width > properties.maximumImageDimension1D) {
                throw new IllegalArgumentException(String.format("Cannot create 1d texture with width (%d) in excess of 'maximumImageDimension1D' (%d)", width, properties.maximumImageDimension1D));
            }
        } else if (depthOrLayers == 1) {
            // 2d texture
            if (width > properties.maximumImageDimension2D) {
                throw new IllegalArgumentException(String.format("Cannot create 2d texture with width (%d) in excess of 'maximumImageDimension2D' (%d)", width, properties.maximumImageDimension2D));
            }
            if (height > properties.maximumImageDimension2D) {
                throw new IllegalArgumentException(String.format("Cannot create 2d texture with height (%d) in excess of 'maximumImageDimension2D' (%d)", height, properties.maximumImageDimension2D));
            }
        } else if ((usage & GpuTexture.USAGE_CUBEMAP_COMPATIBLE) != 0) {
            // cubemap
            if (width > properties.maximumImageDimensionCube) {
                throw new IllegalArgumentException(String.format("Cannot create cubemap texture with height (%d) in excess of 'maximumImageDimensionCube' (%d)", width, properties.maximumImageDimensionCube));
            }
            if (height > properties.maximumImageDimensionCube) {
                throw new IllegalArgumentException(String.format("Cannot create cubemap texture with height (%d) in excess of 'maximumImageDimensionCube' (%d)", height, properties.maximumImageDimensionCube));
            }
        }
        // no need to check 3d textures, currently B3D will throw for that
    }

    void validateAddressMode(AddressMode mode) {
        if (!properties.knownAddressModes.contains(mode)) {
            throw new IllegalArgumentException(String.format("Unknown by device AddressMode (%s) provided, known formats are %s", mode.name(), Arrays.toString(properties.knownAddressModes.toArray())));
        }
    }

    public void validatePipeline(RenderPipeline pipeline) {
        if (pipeline.getColorLogic() != LogicOp.NONE && enabledFeatures.logicOp()) {
            throw new IllegalArgumentException(String.format("Cannot use LogicOp other than NONE without enabling 'logicOp' device feature, %s", pipeline.getLocation()));
        }
        if (pipeline.getVertexFormat().getElements().size() > properties.maximumVertexInputAttributes) {
            throw new IllegalArgumentException(String.format("Cannot use more than %d vertex input attributes, %s attempted to use %d attributes", properties.maximumVertexInputAttributes, pipeline.getLocation(), pipeline.getVertexFormat().getElements().size()));
        }
        int ubos = 0;
        int utbs = 0;
        final int samplers = pipeline.getSamplers().size();
        for (RenderPipeline.UniformDescription uniform : pipeline.getUniforms()) {
            if (!properties.knownUniformTypes.contains(uniform.type())) {
                throw new IllegalArgumentException(String.format("Unknown by device UniformType (%s) provided for uniform %s, known types are %s", uniform.type().name(), uniform.name(), Arrays.toString(properties.knownUniformTypes.toArray())));
            }
            switch (uniform.type()) {
                case UNIFORM_BUFFER -> ubos++;
                case TEXEL_BUFFER -> utbs++;
            }
        }
        if (ubos > properties.maximumUBOBindings) {
            throw new IllegalArgumentException(String.format("Pipeline %s defines %s uniform buffers, more than %s allowed", pipeline.getLocation(), ubos, properties.maximumUBOBindings));
        }
        if (utbs + samplers > properties.maximumTextureBindings) {
            throw new IllegalArgumentException(String.format("Pipeline %s defines %s texture bindings, more than %s allowed", pipeline.getLocation(), utbs + samplers, properties.maximumTextureBindings));
        }
        if (ubos + utbs + samplers > properties.maximumTextureBindings) {
            throw new IllegalArgumentException(String.format("Pipeline %s defines %s uniform bindings, more than %s allowed", pipeline.getLocation(), ubos + utbs + samplers, properties.maximumUniformBindings));
        }
        if (!properties.knownDepthTestFunctions.contains(pipeline.getDepthTestFunction())) {
            throw new IllegalArgumentException(String.format("Unknown by device DepthTestFunction (%s) provided, known functions are %s", pipeline.getDepthTestFunction().name(), Arrays.toString(properties.knownDepthTestFunctions.toArray())));
        }
        final var blendFunctionOpt = pipeline.getBlendFunction();
        if (blendFunctionOpt.isPresent()) {
            final var blendFunc = blendFunctionOpt.get();
            if (!properties.knownSourceFactors.contains(blendFunc.sourceColor())) {
                throw new IllegalArgumentException(String.format("Unknown by device SourceFactor (%s) provided for sourceColor, known factors are %s", blendFunc.sourceColor().name(), Arrays.toString(properties.knownSourceFactors.toArray())));
            }
            if (!properties.knownSourceFactors.contains(blendFunc.sourceAlpha())) {
                throw new IllegalArgumentException(String.format("Unknown by device SourceFactor (%s) provided for sourceAlpha, known factors are %s", blendFunc.sourceAlpha().name(), Arrays.toString(properties.knownSourceFactors.toArray())));
            }
            if (!properties.knownDestFactors.contains(blendFunc.destColor())) {
                throw new IllegalArgumentException(String.format("Unknown by device SourceFactor (%s) provided for destColor, known factors are %s", blendFunc.destColor().name(), Arrays.toString(properties.knownDestFactors.toArray())));
            }
            if (!properties.knownDestFactors.contains(blendFunc.destAlpha())) {
                throw new IllegalArgumentException(String.format("Unknown by device SourceFactor (%s) provided for destAlpha, known factors are %s", blendFunc.destAlpha().name(), Arrays.toString(properties.knownDestFactors.toArray())));
            }
        }
    }

    public void validateUBOBinding(GpuBufferSlice slice) {
        if (slice.offset() % properties.minimumUBOAlignment != 0) {
            throw new IllegalArgumentException(String.format("Cannot bind misaligned UBO slice, %d alignment mismatch from required %d multiple", slice.offset() % properties.minimumUBOAlignment, properties.minimumUBOAlignment));
        }
        if (slice.length() > properties.maximumUBOSize) {
            throw new IllegalArgumentException(String.format("Cannot bind UBO of size %d, limited to %d", slice.length(), properties.maximumUBOSize));
        }
    }
}
