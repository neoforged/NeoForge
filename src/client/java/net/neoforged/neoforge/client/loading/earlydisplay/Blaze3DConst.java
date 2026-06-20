package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.buffers.GpuBuffer;
import net.neoforged.fml.earlydisplay.render.backend.ELSBuffer;

import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
final class Blaze3DConst {
    static int elsUsageToB3D(Set<ELSBuffer.Usage> usage) {
        int mask = 0;
        for (ELSBuffer.Usage entry : usage) {
            mask |= switch (entry) {
                case MAP_READ -> GpuBuffer.USAGE_MAP_READ;
                case MAP_WRITE -> GpuBuffer.USAGE_MAP_WRITE;
                case HINT_CLIENT_STORAGE -> GpuBuffer.USAGE_HINT_CLIENT_STORAGE;
                case COPY_DST -> GpuBuffer.USAGE_COPY_DST;
                case COPY_SRC -> GpuBuffer.USAGE_COPY_SRC;
                case VERTEX -> GpuBuffer.USAGE_VERTEX;
                case INDEX -> GpuBuffer.USAGE_INDEX;
                case UNIFORM -> GpuBuffer.USAGE_UNIFORM;
            };
        }
        return mask;
    }

    private Blaze3DConst() { }
}
