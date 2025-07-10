/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d;

import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.Set;

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
}
