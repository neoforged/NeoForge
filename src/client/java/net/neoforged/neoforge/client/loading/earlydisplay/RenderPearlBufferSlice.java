/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.neoforged.fml.earlydisplay.render.backend.ELSBufferSlice;

@SuppressWarnings("UnstableApiUsage")
final class RenderPearlBufferSlice implements ELSBufferSlice {
    private final RenderPearlBuffer buffer;
    private final GpuBufferSlice b3dBufferSlice;

    RenderPearlBufferSlice(RenderPearlBuffer buffer, GpuBufferSlice b3dBufferSlice) {
        this.buffer = buffer;
        this.b3dBufferSlice = b3dBufferSlice;
    }

    @Override
    public RenderPearlBuffer buffer() {
        return this.buffer;
    }

    @Override
    public long offset() {
        return this.b3dBufferSlice.offset();
    }

    @Override
    public long length() {
        return this.b3dBufferSlice.length();
    }

    GpuBufferSlice unwrap() {
        return this.b3dBufferSlice;
    }
}
