/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import java.util.Set;
import net.neoforged.fml.earlydisplay.render.backend.ELSBuffer;

@SuppressWarnings("UnstableApiUsage")
final class RenderPearlBuffer implements ELSBuffer {
    private final GpuBuffer b3dBuffer;
    private final Set<Usage> usage;
    private final int usageMask;
    private final RenderPearlBufferSlice defaultSlice;

    RenderPearlBuffer(GpuBuffer b3dBuffer, Set<Usage> usage, int usageMask) {
        this.b3dBuffer = b3dBuffer;
        this.usage = usage;
        this.usageMask = usageMask;
        this.defaultSlice = new RenderPearlBufferSlice(this, b3dBuffer.slice(0, b3dBuffer.size()));
    }

    @Override
    public Set<Usage> usage() {
        return this.usage;
    }

    @Override
    public long size() {
        return this.b3dBuffer.size();
    }

    @Override
    public RenderPearlBufferSlice slice() {
        return this.defaultSlice;
    }

    @Override
    public RenderPearlBufferSlice slice(long offset, long length) {
        return new RenderPearlBufferSlice(this, this.b3dBuffer.slice(offset, length));
    }

    @Override
    public void close() {
        this.b3dBuffer.close();
    }

    int getUsageMask() {
        return this.usageMask;
    }

    GpuBuffer unwrap() {
        return this.b3dBuffer;
    }
}
