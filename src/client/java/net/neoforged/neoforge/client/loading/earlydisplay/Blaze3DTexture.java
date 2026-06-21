/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.neoforged.fml.earlydisplay.render.backend.ELSTexture;
import net.neoforged.fml.earlydisplay.render.backend.TextureFormat;

@SuppressWarnings("UnstableApiUsage")
final class Blaze3DTexture implements ELSTexture {
    private final TextureFormat format;
    private final GpuTexture b3dTexture;
    private final GpuTextureView b3dTextureView;
    final GpuSampler sampler;

    Blaze3DTexture(TextureFormat format, GpuTexture b3dTexture, GpuTextureView b3dTextureView, GpuSampler sampler) {
        this.format = format;
        this.b3dTexture = b3dTexture;
        this.b3dTextureView = b3dTextureView;
        this.sampler = sampler;
    }

    @Override
    public int width() {
        return this.b3dTexture.getWidth(0);
    }

    @Override
    public int height() {
        return this.b3dTexture.getHeight(0);
    }

    @Override
    public TextureFormat format() {
        return this.format;
    }

    GpuTexture unwrap() {
        return this.b3dTexture;
    }

    GpuTextureView view() {
        return this.b3dTextureView;
    }

    @Override
    public void close() {
        this.b3dTextureView.close();
        this.b3dTexture.close();
    }
}
