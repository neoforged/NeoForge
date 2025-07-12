/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d;

import net.neoforged.neoforge.client.event.ConfigureGpuDeviceEvent;

public interface GpuDeviceExtension {
    /**
     * GpuDeviceProperties representative of features enabled during device configuration.
     * @see GpuDeviceProperties
     * @see ConfigureGpuDeviceEvent
     */
    GpuDeviceProperties enabledProperties();

    /**
     * GpuDeviceFeatures enabled during device configuration.
     * @see GpuDeviceFeatures
     * @see ConfigureGpuDeviceEvent
     */
    GpuDeviceFeatures enabledFeatures();
}
