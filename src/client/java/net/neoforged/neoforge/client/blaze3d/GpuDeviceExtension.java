/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d;

public interface GpuDeviceExtension {
    GpuDeviceProperties enabledProperties();

    GpuDeviceFeatures enabledFeatures();
}
