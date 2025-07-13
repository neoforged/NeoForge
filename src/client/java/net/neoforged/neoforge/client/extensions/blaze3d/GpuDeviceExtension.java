/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions.blaze3d;

import net.neoforged.neoforge.client.blaze3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceProperties;
import net.neoforged.neoforge.client.event.ConfigureGpuDeviceEvent;

public interface GpuDeviceExtension {
    /**
     * @see GpuDeviceProperties
     */
    GpuDeviceProperties deviceProperties();

    /**
     * GpuDeviceFeatures enabled during device configuration.
     * 
     * @see GpuDeviceFeatures
     * @see ConfigureGpuDeviceEvent
     */
    GpuDeviceFeatures enabledFeatures();
}
