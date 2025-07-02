/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.neo3d.validation;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import org.jetbrains.annotations.ApiStatus;

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
    public void setAddressMode(AddressMode addressModeU, AddressMode addressModeV) {
        validator.validateAddressMode(addressModeU);
        validator.validateAddressMode(addressModeV);
        super.setAddressMode(addressModeU, addressModeV);
        realTexture.setAddressMode(addressModeU, addressModeV);
    }

    @Override
    public void setTextureFilter(FilterMode minFilter, FilterMode magFilter, boolean useMipmaps) {
        super.setTextureFilter(magFilter, magFilter, useMipmaps);
        realTexture.setTextureFilter(magFilter, magFilter, useMipmaps);
    }

    @Override
    public void setUseMipmaps(boolean useMipmaps) {
        super.setUseMipmaps(useMipmaps);
        realTexture.setUseMipmaps(useMipmaps);
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
