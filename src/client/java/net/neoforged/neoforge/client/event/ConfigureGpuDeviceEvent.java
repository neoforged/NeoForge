/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.neo3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.neo3d.GpuDeviceProperties;

public class ConfigureGpuDeviceEvent extends Event implements IModBusEvent {
    private final GpuDeviceProperties deviceProperties;
    private final GpuDeviceFeatures availableFeatures;
    private final GpuDeviceFeatures enabledFeatures;

    public ConfigureGpuDeviceEvent(GpuDeviceProperties deviceProperties, GpuDeviceFeatures availableFeatures, GpuDeviceFeatures enabledFeatures) {
        this.deviceProperties = deviceProperties;
        this.availableFeatures = availableFeatures;
        this.enabledFeatures = enabledFeatures;
    }

    public GpuDeviceProperties getDeviceProperties() {
        return deviceProperties;
    }

    public GpuDeviceFeatures getAvailableFeatures() {
        return availableFeatures.clone();
    }

    public GpuDeviceFeatures getEnabledFeatures() {
        return enabledFeatures.clone();
    }

    public void requireFeatures(GpuDeviceFeatures requiredFeatures) {
        if (!availableFeatures.hasAll(requiredFeatures)) {
            throw new IllegalArgumentException("Attempted to enable unavailable GpuDevice feature");
        }
        enabledFeatures.enableAll(requiredFeatures);
    }
}
