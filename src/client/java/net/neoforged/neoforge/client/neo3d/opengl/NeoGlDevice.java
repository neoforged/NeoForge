/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d.opengl;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.shaders.ShaderType;
import java.util.function.BiFunction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.ConfigureGpuDeviceEvent;
import net.neoforged.neoforge.client.neo3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.neo3d.GpuDeviceProperties;

public class NeoGlDevice extends GlDevice {
    private final GpuDeviceProperties deviceProperties;
    private final GpuDeviceFeatures enabledFeatures;

    public NeoGlDevice(long window, int debugLevel, boolean syncDebug, BiFunction<ResourceLocation, ShaderType, String> defaultShaderSource, boolean enableDebugLabels) {
        super(window, debugLevel, syncDebug, defaultShaderSource, enableDebugLabels);

        deviceProperties = new GpuDeviceProperties.Immutable(new NeoGlDeviceProperties());
        final var event = ModLoader.postEventWithReturn(new ConfigureGpuDeviceEvent(properties(), new NeoGlDeviceFeatures()));
        enabledFeatures = new GpuDeviceFeatures.Immutable(event);
    }

    @Override
    public GpuDeviceProperties properties() {
        return deviceProperties;
    }

    @Override
    public GpuDeviceFeatures enabledFeatures() {
        return enabledFeatures;
    }
}
