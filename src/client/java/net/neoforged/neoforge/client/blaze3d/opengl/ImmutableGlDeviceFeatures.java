package net.neoforged.neoforge.client.blaze3d.opengl;

import net.neoforged.neoforge.client.blaze3d.GpuDeviceFeatures;
import org.jetbrains.annotations.ApiStatus;

record ImmutableGlDeviceFeatures(
        boolean logicOp) implements GpuDeviceFeatures {
    ImmutableGlDeviceFeatures(GpuDeviceFeatures features) {
        this(features.logicOp());
    }
}
