/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.textures.GpuTexture;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceFeatures;
import net.neoforged.neoforge.client.blaze3d.GpuDeviceProperties;

/**
 * Helper class for validations done by the ValidationGpuDevice layer
 */
public class GpuDeviceUsageValidator {
    private final GpuDeviceProperties properties;
    private final GpuDeviceFeatures enabledFeatures;
    private final boolean checkReservedUsageBits;

    GpuDeviceUsageValidator(ValidationGpuDevice device, boolean checkReservedUsageBits) {
        properties = device.deviceProperties();
        enabledFeatures = device.enabledFeatures();
        this.checkReservedUsageBits = checkReservedUsageBits;
    }

    void validateBufferUsage(int usage) {
        if (checkReservedUsageBits) {
            if ((usage & GpuBuffer.RESERVED_USAGE_BITS) != 0) {
                throw new IllegalArgumentException("Use of reserved GpuBuffer usage bits with a backend that does not utilize them.");
            }
        }
        // the reserved bits were already checked, so ignore those
        usage &= ~GpuBuffer.RESERVED_USAGE_BITS;
        var knownGpuBufferBits = (GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER << 1) - 1;
        if ((usage & ~knownGpuBufferBits) != 0) {
            throw new IllegalArgumentException("Use of undefined GpuBuffer usage bits");
        }
        // support all other bits are required, no further validation necessary
    }

    void validateTextureUsage(int usage) {
        if (checkReservedUsageBits) {
            if ((usage & GpuTexture.RESERVED_USAGE_BITS) != 0) {
                throw new IllegalArgumentException("Use of reserved GpuTexture usage bits with a backend that does not utilize them.");
            }
        }
        // the reserved bits were already checked, so ignore those
        usage &= ~GpuTexture.RESERVED_USAGE_BITS;

        var knownGpuTextureBits = (GpuTexture.USAGE_CUBEMAP_COMPATIBLE << 1) - 1;
        if ((usage & ~knownGpuTextureBits) != 0) {
            throw new IllegalArgumentException("Use of undefined GpuTexture usage bits");
        }
        // support all other bits are required, no further validation necessary
    }

    void validatePipeline(RenderPipeline pipeline) {
        if (pipeline.getColorLogic() != LogicOp.NONE && !enabledFeatures.logicOp()) {
            throw new IllegalArgumentException(String.format("Cannot use LogicOp other than NONE without enabling 'logicOp' device feature, %s", pipeline.getLocation()));
        }
    }
}
