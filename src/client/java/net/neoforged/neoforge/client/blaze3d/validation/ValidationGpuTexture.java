/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.validation;

import com.mojang.blaze3d.textures.GpuTexture;
import org.jetbrains.annotations.ApiStatus;

/**
 * Validation wrapper around GpuTexture
 * <br>
 * Currently this class is boilerplate
 */
public class ValidationGpuTexture extends GpuTexture {
    private final GpuTexture realTexture;
    private final GpuDeviceUsageValidator validator;

    public ValidationGpuTexture(GpuTexture realTexture, GpuDeviceUsageValidator validator) {
        super(realTexture.usage(), realTexture.getLabel(), realTexture.getFormat(), realTexture.getWidth(0), realTexture.getHeight(0), realTexture.getDepthOrLayers(),
                realTexture.getMipLevels());
        this.realTexture = realTexture;
        this.validator = validator;
    }

    @ApiStatus.Internal
    public GpuTexture getRealTexture() {
        return realTexture;
    }

    @Override
    public void close() {
        realTexture.close();
    }

    @Override
    public boolean isClosed() {
        return realTexture.isClosed();
    }
}
