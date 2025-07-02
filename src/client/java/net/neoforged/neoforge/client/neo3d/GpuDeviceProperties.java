/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class GpuDeviceProperties {
    public final String backendName = defaultVal("Default");
    public final String apiName = defaultVal("OpenGL");
    public final String apiVersionString = defaultVal("3.2");
    public final int apiVersionMajor = defaultVal(3);
    public final int apiVersionMinor = defaultVal(2);

    public final int knownGpuBufferUsageBits = defaultVal((GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER << 1) - 1);
    public final int knownGpuTextureUsageBits = defaultVal((GpuTexture.USAGE_CUBEMAP_COMPATIBLE << 1) - 1);
    public final Set<DepthTestFunction> knownDepthTestFunctions = defaultVal(Collections.unmodifiableSet(EnumSet.allOf(DepthTestFunction.class)));
    public final Set<DestFactor> knownDestFactors = defaultVal(Collections.unmodifiableSet(EnumSet.allOf(DestFactor.class)));
    public final Set<SourceFactor> knownSourceFactors = defaultVal(Collections.unmodifiableSet(EnumSet.allOf(SourceFactor.class)));
    public final Set<ShaderType> knownShaderTypes = defaultVal(Collections.unmodifiableSet(EnumSet.allOf(ShaderType.class)));
    public final Set<UniformType> knownUniformTypes = defaultVal(Collections.unmodifiableSet(EnumSet.allOf(UniformType.class)));
    public final Set<AddressMode> knownAddressModes = defaultVal(Collections.unmodifiableSet(EnumSet.allOf(AddressMode.class)));
    public final Set<TextureFormat> knownTextureFormats = defaultVal(Collections.unmodifiableSet(EnumSet.allOf(TextureFormat.class)));

    /**
     * Some APIs require a 0-1 depth range, this may affect shaders that rely on specific depth ranges
     */
    public final boolean depthZeroToOne = defaultVal(false);

    /**
     * Total uniforms that can be used by a single program
     * The sum of the per-type maximums may exceed this value
     */
    public final int maximumUniformBindings = atLeast(16);
    /**
     * min(GL_MAX_VERTEX_UNIFORM_BLOCKS, GL_MAX_FRAGMENT_UNIFORM_BLOCKS)
     */
    public final int maximumUBOBindings = atLeast(12);
    /**
     * Maximum number of textures that can be bound to a single pipeline.
     * TexelBuffers also count against this limit.
     * min(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, GL_MAX_TEXTURE_IMAGE_UNITS)
     */
    public final int maximumTextureBindings = atLeast(16);

    /**
     * GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT
     * GpuDevice#getUniformOffsetAlignment()
     * This value is known to be greater than 1 on many GPUs
     */
    public final int minimumUBOAlignment = minimumPossible(1);
    /**
     * GL_MAX_UNIFORM_BLOCK_SIZE
     */
    public final int maximumUBOSize = atLeast(16384);
    /**
     * GL_MAX_TEXTURE_BUFFER_SIZE
     */
    public final int maximumTexelBufferSize = atLeast(65536);

    /**
     * GL_MAX_VERTEX_ATTRIBS
     */
    public final int maximumVertexInputAttributes = atLeast(16);
    /**
     * GL_MAX_VERTEX_ATTRIB_BINDINGS
     */
    public final int maximumVertexInputBuffers = atLeast(16);
    /**
     * GL_MAX_VERTEX_OUTPUT_COMPONENTS / 4
     */
    public final int maximumVertexOutputLocations = atLeast(16);

    /**
     * GL_MAX_ARRAY_TEXTURE_LAYERS
     */
    public final int maximumImageArrayLayers = atLeast(256);
    /**
     * GL_MAX_TEXTURE_SIZE
     */
    public final int maximumImageDimension1D = atLeast(1024);
    /**
     * GL_MAX_TEXTURE_SIZE
     */
    public final int maximumImageDimension2D = atLeast(1024);
    /**
     * GL_MAX_3D_TEXTURE_SIZE
     */
    public final int maximumImageDimension3D = atLeast(1024);
    /**
     * GL_MAX_CUBE_MAP_TEXTURE_SIZE
     */
    public final int maximumImageDimensionCube = atLeast(1024);

    public static <T extends GpuDeviceProperties> T create(Class<T> clazz, Map<String, Object> values) {
        try {
            final var instance = clazz.getDeclaredConstructor().newInstance();

            final var fields = new ReferenceArrayList<Field>();
            var subClass = (Class<?>) clazz;
            while (subClass != Object.class) {
                fields.addAll(Arrays.asList(subClass.getFields()));
                subClass = clazz.getSuperclass();
            }
            final var fieldMap = new Object2ReferenceOpenHashMap<String, Field>();
            for (Field field : fields) {
                fieldMap.put(field.getName(), field);
            }
            values.forEach((name, value) -> {
                try {
                    final var field = fieldMap.get(name);
                    if (field != null) {
                        field.setAccessible(true);
                        field.set(instance, value);
                    }
                } catch (IllegalAccessException ignored) {}
            });

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    // tricks the compiler into not inlining the values, also allows some context to be added for each item
    protected <T> T defaultVal(T t) {
        return t;
    }

    protected <T> T atLeast(T t) {
        return t;
    }

    protected <T> T minimumPossible(T t) {
        return t;
    }
}
