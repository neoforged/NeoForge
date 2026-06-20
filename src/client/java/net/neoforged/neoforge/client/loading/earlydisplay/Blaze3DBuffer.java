package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.buffers.GpuBuffer;
import net.neoforged.fml.earlydisplay.render.backend.ELSBuffer;

import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
final class Blaze3DBuffer implements ELSBuffer {
    private final GpuBuffer b3dBuffer;
    private final Set<Usage> usage;
    private final int usageMask;
    private final Blaze3DBufferSlice defaultSlice;

    Blaze3DBuffer(GpuBuffer b3dBuffer, Set<Usage> usage, int usageMask) {
        this.b3dBuffer = b3dBuffer;
        this.usage = usage;
        this.usageMask = usageMask;
        this.defaultSlice = new Blaze3DBufferSlice(this, b3dBuffer.slice(0, b3dBuffer.size()));
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
    public Blaze3DBufferSlice slice() {
        return this.defaultSlice;
    }

    @Override
    public Blaze3DBufferSlice slice(long offset, long length) {
        return new Blaze3DBufferSlice(this, this.b3dBuffer.slice(offset, length));
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
