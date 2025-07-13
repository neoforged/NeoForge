/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceProperties;
import net.neoforged.neoforge.client.extensions.blaze3d.GpuDeviceExtension;

/**
 * Fired during GpuDevice creation to allow optional features to be enabled.
 * 
 * @see GpuDeviceFeatures
 * @see GpuDeviceProperties
 * @see GpuDeviceExtension
 */
public class ConfigureGpuDeviceEvent extends Event implements IModBusEvent, GpuDeviceFeatures {
    private final GpuDeviceProperties deviceProperties;
    private final GpuDeviceFeatures availableFeatures;

    private boolean logicOp = false;

    public ConfigureGpuDeviceEvent(GpuDeviceProperties deviceProperties, GpuDeviceFeatures availableFeatures) {
        this.deviceProperties = deviceProperties;
        this.availableFeatures = availableFeatures;
    }

    /**
     * Device features available.
     * <br>
     * This is constant for the entire GpuDevice's lifetime and may be cached for future use.
     */
    public GpuDeviceProperties getDeviceProperties() {
        return deviceProperties;
    }

    /**
     * Device features available for use, use this to adapt what features your mod enables and uses based on what is available.
     */
    public GpuDeviceFeatures getAvailableFeatures() {
        return availableFeatures;
    }

    @Override
    public boolean logicOp() {
        return logicOp;
    }

    /**
     * Enables the LogicOp feature.
     * <p>
     * Allows a {@link com.mojang.blaze3d.platform.LogicOp} other than {@link com.mojang.blaze3d.platform.LogicOp#NONE} to be specified when using a {@link com.mojang.blaze3d.pipeline.RenderPipeline}.
     */
    public void enableLogicOp() {
        if (!availableFeatures.logicOp()) {
            throw new UnsupportedOperationException("LogicOp is unavailable");
        }
        logicOp = true;
    }
}
