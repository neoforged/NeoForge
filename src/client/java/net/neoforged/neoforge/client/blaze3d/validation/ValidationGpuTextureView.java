/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.blaze3d.validation;

import com.mojang.blaze3d.textures.GpuTextureView;

public class ValidationGpuTextureView extends GpuTextureView {
    private final GpuTextureView realTextureView;

    public ValidationGpuTextureView(ValidationGpuTexture validationGpuTexture, GpuTextureView realTextureView, GpuDeviceUsageValidator validator) {
        super(validationGpuTexture, realTextureView.baseMipLevel(), realTextureView.mipLevels());
        this.realTextureView = realTextureView;
    }

    public GpuTextureView getRealTextureView() {
        return realTextureView;
    }

    @Override
    public void close() {
        realTextureView.close();
    }

    @Override
    public boolean isClosed() {
        return realTextureView.isClosed();
    }
}
