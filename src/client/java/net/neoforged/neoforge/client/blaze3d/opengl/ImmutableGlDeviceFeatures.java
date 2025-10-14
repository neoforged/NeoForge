/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.opengl;

import net.neoforged.neoforge.client.blaze3d.GpuDeviceFeatures;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ImmutableGlDeviceFeatures(
        boolean logicOp) implements GpuDeviceFeatures {
    public ImmutableGlDeviceFeatures(GpuDeviceFeatures features) {
        this(features.logicOp());
    }
}
