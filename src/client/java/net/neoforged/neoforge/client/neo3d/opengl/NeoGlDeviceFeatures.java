/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d.opengl;

import net.minecraft.Util;
import net.neoforged.neoforge.client.neo3d.GpuDeviceFeatures;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class NeoGlDeviceFeatures implements GpuDeviceFeatures {
    private final NeoGlDeviceFeatures availableFeatures;
    private boolean locked = false;

    private boolean logicOp = false;

    public NeoGlDeviceFeatures(@Nullable NeoGlDeviceFeatures availableFeatures) {
        if (availableFeatures == null) {
            querySupport();
            this.availableFeatures = this;
        } else {
            this.availableFeatures = availableFeatures;
        }
    }

    private void querySupport() {
        lock();
        logicOp = !(Util.getPlatform() == Util.OS.WINDOWS && Util.isAarch64());
    }

    @ApiStatus.Internal
    public void lock() {
        locked = true;
    }

    void checkLocked() {
        if (locked) {
            throw new IllegalStateException("Cannot set features on locked feature set");
        }
    }

    @Override
    public boolean logicOp() {
        return logicOp;
    }

    @Override
    public void enableLogicOp() {
        checkLocked();
        if (!availableFeatures.logicOp()) {
            throw new UnsupportedOperationException("LogicOp is unavailable");
        }
        logicOp = true;
    }
}
