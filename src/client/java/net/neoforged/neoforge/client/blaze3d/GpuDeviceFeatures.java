/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d;

public interface GpuDeviceFeatures {
    /**
     * LogicOp is unusably problematic on Qualcomm GPUs via OpenGL
     * LogicOp is unavailable on MacOS via Vulkan
     */
    boolean logicOp();
}
