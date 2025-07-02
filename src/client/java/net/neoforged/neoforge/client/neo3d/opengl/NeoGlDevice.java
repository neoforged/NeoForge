/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d.opengl;

import static org.lwjgl.opengl.ARBVertexAttribBinding.GL_MAX_VERTEX_ATTRIB_BINDINGS;
import static org.lwjgl.opengl.GL11C.GL_MAX_TEXTURE_SIZE;
import static org.lwjgl.opengl.GL11C.glGetInteger;
import static org.lwjgl.opengl.GL12C.GL_MAX_3D_TEXTURE_SIZE;
import static org.lwjgl.opengl.GL13C.GL_MAX_CUBE_MAP_TEXTURE_SIZE;
import static org.lwjgl.opengl.GL20C.GL_MAX_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.opengl.GL20C.GL_MAX_VERTEX_ATTRIBS;
import static org.lwjgl.opengl.GL20C.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.opengl.GL30C.GL_MAJOR_VERSION;
import static org.lwjgl.opengl.GL30C.GL_MAX_ARRAY_TEXTURE_LAYERS;
import static org.lwjgl.opengl.GL30C.GL_MINOR_VERSION;
import static org.lwjgl.opengl.GL31C.GL_MAX_FRAGMENT_UNIFORM_BLOCKS;
import static org.lwjgl.opengl.GL31C.GL_MAX_TEXTURE_BUFFER_SIZE;
import static org.lwjgl.opengl.GL31C.GL_MAX_UNIFORM_BLOCK_SIZE;
import static org.lwjgl.opengl.GL31C.GL_MAX_VERTEX_UNIFORM_BLOCKS;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static org.lwjgl.opengl.GL32C.GL_MAX_VERTEX_OUTPUT_COMPONENTS;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.textures.GpuTexture;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import java.util.function.BiFunction;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.ConfigureGpuDeviceEvent;
import net.neoforged.neoforge.client.neo3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.neo3d.GpuDeviceProperties;
import org.lwjgl.opengl.GL;

public class NeoGlDevice extends GlDevice {
    private final GpuDeviceProperties deviceProperties;
    private final GpuDeviceFeatures deviceFeatures;

    public NeoGlDevice(long window, int debugLevel, boolean syncDebug, BiFunction<ResourceLocation, ShaderType, String> defaultShaderSource, boolean enableDebugLabels) {
        super(window, debugLevel, syncDebug, defaultShaderSource, enableDebugLabels);

        final var propertiesMap = new Object2ReferenceOpenHashMap<String, Object>();
        final var glCaps = GL.getCapabilities();

        final var glMajorVersion = glGetInteger(GL_MAJOR_VERSION);
        final var glMinorVersion = glGetInteger(GL_MINOR_VERSION);
        propertiesMap.put("apiVersionString", String.format("%d.%d", glMajorVersion, glMinorVersion));
        propertiesMap.put("apiVersionMajor", glMajorVersion);
        propertiesMap.put("apiVersionMinor", glMinorVersion);

        propertiesMap.put("knownGpuBufferUsageBits", (GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER << 1) - 1);
        propertiesMap.put("knownGpuTextureUsageBits", (GpuTexture.USAGE_CUBEMAP_COMPATIBLE << 1) - 1);
        
        propertiesMap.put("depthZeroToOne", false);

        final var maxUboBindings = Math.min(glGetInteger(GL_MAX_VERTEX_UNIFORM_BLOCKS), glGetInteger(GL_MAX_FRAGMENT_UNIFORM_BLOCKS));
        final var maxTextureBindings = Math.min(glGetInteger(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS), glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS));
        // with GL, these don't overlap, so, the max total is the sum
        propertiesMap.put("maximumUniformBindings", maxUboBindings + maxTextureBindings);
        propertiesMap.put("maximumUBOBindings", maxUboBindings);
        propertiesMap.put("maximumTextureBindings", maxTextureBindings);

        propertiesMap.put("minimumUBOAlignment", glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT));
        propertiesMap.put("maximumUBOSize", glGetInteger(GL_MAX_UNIFORM_BLOCK_SIZE));
        propertiesMap.put("maximumTexelBufferSize", glGetInteger(GL_MAX_TEXTURE_BUFFER_SIZE));

        propertiesMap.put("maximumVertexInputAttributes", glGetInteger(GL_MAX_VERTEX_ATTRIBS));
        if (glCaps.GL_ARB_vertex_attrib_binding) {
            propertiesMap.put("maximumVertexInputBuffers", glGetInteger(GL_MAX_VERTEX_ATTRIB_BINDINGS));
        } else {
            // older GL doesn't specify, but as many attribs is a valid limit in that case
            propertiesMap.put("maximumVertexInputBuffers", glGetInteger(GL_MAX_VERTEX_ATTRIBS));
        }
        propertiesMap.put("maximumVertexOutputLocations", glGetInteger(GL_MAX_VERTEX_OUTPUT_COMPONENTS) / 4);

        propertiesMap.put("maximumImageArrayLayers", glGetInteger(GL_MAX_ARRAY_TEXTURE_LAYERS));
        propertiesMap.put("maximumImageDimension1D", glGetInteger(GL_MAX_TEXTURE_SIZE));
        propertiesMap.put("maximumImageDimension2D", glGetInteger(GL_MAX_TEXTURE_SIZE));
        propertiesMap.put("maximumImageDimension3D", glGetInteger(GL_MAX_3D_TEXTURE_SIZE));
        propertiesMap.put("maximumImageDimensionCube", glGetInteger(GL_MAX_CUBE_MAP_TEXTURE_SIZE));

        deviceProperties = GpuDeviceProperties.create(GpuDeviceProperties.class, propertiesMap);
        final var availableFeatures = new GpuDeviceFeatures();
        // logic op is unavailable on 
        availableFeatures.logicOp = !(Util.getPlatform() == Util.OS.WINDOWS && Util.isAarch64());
        final var configureResult = ModLoader.postEventWithReturn(new ConfigureGpuDeviceEvent(properties(), availableFeatures, new GpuDeviceFeatures()));
        deviceFeatures = configureResult.getEnabledFeatures();
    }

    @Override
    public GpuDeviceProperties properties() {
        return deviceProperties;
    }

    @Override
    public GpuDeviceFeatures enabledFeatures() {
        // because the objects are mutable, return a clone
        return deviceFeatures.clone();
    }
}
