/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d.opengl;

import net.minecraft.Util;
import net.neoforged.neoforge.client.neo3d.GpuDeviceFeatures;

public class NeoGlDeviceFeatures implements GpuDeviceFeatures {
    @Override
    public boolean logicOp() {
        return !(Util.getPlatform() == Util.OS.WINDOWS && Util.isAarch64());
    }
}
