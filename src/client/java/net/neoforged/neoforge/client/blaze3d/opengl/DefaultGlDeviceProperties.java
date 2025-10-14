/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.opengl;

import net.neoforged.neoforge.client.blaze3d.GpuDeviceProperties;

/**
 * Theoretical GpuDevice properties for the default GlDevice
 */
public class DefaultGlDeviceProperties implements GpuDeviceProperties {
    @Override
    public String backendName() {
        return "Default";
    }

    @Override
    public String apiName() {
        return "OpenGL";
    }
}
