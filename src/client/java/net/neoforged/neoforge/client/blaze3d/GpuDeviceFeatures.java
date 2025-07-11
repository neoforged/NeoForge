/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d;

/**
 * Enable-able features of a Blaze3d backend GpuDevice.
 * <br>
 * Not all backends support all features, and some features are known to be problematic on some hardware. For details on those features see notes for each feature.
 * <br>
 * Features must be enabled via the {@link net.neoforged.neoforge.client.event.ConfigureGpuDeviceEvent} to be used.
 */
public interface GpuDeviceFeatures {
    /**
     * LogicOp is unusably problematic on Qualcomm GPUs via OpenGL.
     * LogicOp is unavailable on MacOS via Vulkan.
     */
    boolean logicOp();
}
