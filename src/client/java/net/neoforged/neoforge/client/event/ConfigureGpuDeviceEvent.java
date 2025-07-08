/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.neo3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.neo3d.GpuDeviceProperties;

public class ConfigureGpuDeviceEvent extends Event implements IModBusEvent, GpuDeviceFeatures {
    private final GpuDeviceProperties deviceProperties;
    private final GpuDeviceFeatures availableFeatures;

    private boolean logicOp = false;

    public ConfigureGpuDeviceEvent(GpuDeviceProperties deviceProperties, GpuDeviceFeatures availableFeatures) {
        this.deviceProperties = deviceProperties;
        this.availableFeatures = availableFeatures;
    }

    public GpuDeviceProperties getDeviceProperties() {
        return deviceProperties;
    }

    public GpuDeviceFeatures getAvailableFeatures() {
        return availableFeatures;
    }

    @Override
    public boolean logicOp() {
        return logicOp;
    }

    public void enableLogicOp() {
        if (!availableFeatures.logicOp()) {
            throw new UnsupportedOperationException("LogicOp is unavailable");
        }
        logicOp = true;
    }
}
