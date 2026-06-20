package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.neoforged.fml.earlydisplay.render.backend.ELSBufferSlice;

@SuppressWarnings("UnstableApiUsage")
final class Blaze3DBufferSlice implements ELSBufferSlice {
    private final Blaze3DBuffer buffer;
    private final GpuBufferSlice b3dBufferSlice;

    Blaze3DBufferSlice(Blaze3DBuffer buffer, GpuBufferSlice b3dBufferSlice) {
        this.buffer = buffer;
        this.b3dBufferSlice = b3dBufferSlice;
    }

    @Override
    public Blaze3DBuffer buffer() {
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
