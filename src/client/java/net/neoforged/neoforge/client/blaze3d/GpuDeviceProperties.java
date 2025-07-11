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
import java.util.EnumSet;
import java.util.Set;
import net.neoforged.neoforge.client.event.ConfigureGpuDeviceEvent;

/**
 * Describes device capability limits similar to what glGetInteger or VkPhysicalDeviceLimits would return
 * <br>
 * When retrieved from {@link ConfigureGpuDeviceEvent#getDeviceProperties()}, this is representing theoretical device capabilities, some properties may not be available if features are not enabled
 * This instance and values from it must not be cached and used later, as it is not representative of allowed capabilities
 * <br>
 * When retrieved from {@link GpuDeviceExtension#enabledProperties()} this is representing enabled and allowed device capabilities
 * This instance and values from it may be cached and used later
 */
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

    /**
     * Bits/Enums known by the backend and/or enabled during configuration.
     * <br>
     * Bits or enums unknown by the backend must not be used. These may be defined by a newer NeoForge version than the backend was built against, or the feature may not be enabled.
     * <br>
     * Backends must not return known values as enabled for features that are not enabled, even if they are supported.
     */
    int knownGpuBufferUsageBits();

    /**
     * @see GpuDeviceProperties#knownGpuBufferUsageBits()
     */
    int knownGpuTextureUsageBits();

    /**
     * All sets returned must be unmodifiable, and should not be unique instances
     * <br>
     * Backends are recommended to use {@link EnumSet#of()} rather than {@link EnumSet#range(Enum, Enum)} to ensure that only known values are returned
     * regardless of where future enum additions are added.
     * Use of {@link EnumSet#range(Enum, Enum)} may include an enum constant added between those that did not previously exist being considered known.
     * <br>
     * 
     * @see GpuDeviceProperties#knownGpuBufferUsageBits()
     */
    Set<DepthTestFunction> knownDepthTestFunctions();

    /**
     * @see GpuDeviceProperties#knownDepthTestFunctions()
     */
    Set<DestFactor> knownDestFactors();

    /**
     * @see GpuDeviceProperties#knownDepthTestFunctions()
     */
    Set<SourceFactor> knownSourceFactors();

    /**
     * @see GpuDeviceProperties#knownDepthTestFunctions()
     */
    Set<ShaderType> knownShaderTypes();

    /**
     * @see GpuDeviceProperties#knownDepthTestFunctions()
     */
    Set<UniformType> knownUniformTypes();

    /**
     * @see GpuDeviceProperties#knownDepthTestFunctions()
     */
    Set<AddressMode> knownAddressModes();

    /**
     * @see GpuDeviceProperties#knownDepthTestFunctions()
     */
    Set<TextureFormat> knownTextureFormats();
}
