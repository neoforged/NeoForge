/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d;

public interface GpuDeviceFeatures {
    /**
     * LogicOp is problematic on Qualcomm GPUs via OpenGL
     * LogicOp is unavailable on MacOS via Vulkan
     */
    boolean logicOp();

    void enableLogicOp();
}
