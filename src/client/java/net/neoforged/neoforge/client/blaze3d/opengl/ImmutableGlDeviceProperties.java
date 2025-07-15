/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.opengl;

import net.neoforged.neoforge.client.blaze3d.GpuDeviceProperties;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ImmutableGlDeviceProperties(
        String backendName,
        String apiName) implements GpuDeviceProperties {
    public ImmutableGlDeviceProperties(GpuDeviceProperties properties) {
        this(
                properties.backendName(),
                properties.apiName());
    }
}
