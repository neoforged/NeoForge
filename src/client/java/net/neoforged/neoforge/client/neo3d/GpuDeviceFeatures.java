/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;

public class GpuDeviceFeatures implements Cloneable {
    /**
     * LogicOp is problematic on Qualcomm GPUs via OpenGL
     * LogicOp is unavailable on MacOS via Vulkan
     */
    public boolean logicOp = false;

    private static final Field[] fields = GpuDeviceFeatures.class.getFields();

    public boolean hasAll(GpuDeviceFeatures features) {
        try {
            for (final var field : fields) {
                if (field.accessFlags().contains(AccessFlag.STATIC)) {
                    continue;
                }
                if (!field.getBoolean(features)) {
                    continue;
                }
                if (field.getBoolean(this)) {
                    continue;
                }
                return false;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void enableAll(GpuDeviceFeatures features) {
        try {
            for (final var field : fields) {
                if (field.accessFlags().contains(AccessFlag.STATIC)) {
                    continue;
                }
                if (!field.getBoolean(features)) {
                    continue;
                }
                field.setBoolean(this, true);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GpuDeviceFeatures clone() {
        try {
            return (GpuDeviceFeatures) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
