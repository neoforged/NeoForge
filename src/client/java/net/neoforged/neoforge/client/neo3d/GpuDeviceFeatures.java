/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d;

import org.jetbrains.annotations.ApiStatus;

public interface GpuDeviceFeatures {
    /**
     * LogicOp is unusably problematic on Qualcomm GPUs via OpenGL
     * LogicOp is unavailable on MacOS via Vulkan
     */
    boolean logicOp();

    record Immutable(boolean logicOp) implements GpuDeviceFeatures {
        /**
         * Immutable record should only be constructed from an instance of the interface
         * <br>
         * Additional elements may be added without being considered a breaking change
         *
         * @see Immutable#Immutable(GpuDeviceFeatures)
         */
        @ApiStatus.Internal
        public Immutable {}

        public Immutable(GpuDeviceFeatures features) {
            this(features.logicOp());
        }
    }
}
