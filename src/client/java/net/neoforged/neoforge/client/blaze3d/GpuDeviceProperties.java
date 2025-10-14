/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d;

import net.neoforged.neoforge.client.extensions.blaze3d.GpuDeviceExtension;

/**
 * Describes device capability limits similar to what glGetInteger or VkPhysicalDeviceLimits would return.
 * <br>
 * Properties are constant for a GpuDevice's lifetime, and can be cached from {@link net.neoforged.neoforge.client.event.ConfigureGpuDeviceEvent} instead of {@link GpuDeviceExtension#deviceProperties()}
 */
public interface GpuDeviceProperties {
    /**
     * Name of the backend implementation, can be used to identify backends that further extend B3D.
     */
    String backendName();

    /**
     * The API the backend is using, ex: "OpenGL" or "Vulkan".
     */
    String apiName();
}
