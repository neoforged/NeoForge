/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.opengl;

import net.neoforged.neoforge.client.blaze3d.GpuDeviceFeatures;

record ImmutableGlDeviceFeatures(
        boolean logicOp) implements GpuDeviceFeatures {
    ImmutableGlDeviceFeatures(GpuDeviceFeatures features) {
        this(features.logicOp());
    }
}
