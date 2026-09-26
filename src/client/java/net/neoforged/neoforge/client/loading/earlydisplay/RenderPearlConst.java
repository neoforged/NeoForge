/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import java.util.Set;
import net.neoforged.fml.earlydisplay.render.backend.ELSBuffer;

@SuppressWarnings("UnstableApiUsage")
final class RenderPearlConst {
    @GpuBuffer.Usage
    static int elsUsageToB3D(Set<ELSBuffer.Usage> usage) {
        int mask = 0;
        for (ELSBuffer.Usage entry : usage) {
            mask |= switch (entry) {
                case COPY_DST -> GpuBuffer.USAGE_COPY_DST;
                case COPY_SRC -> GpuBuffer.USAGE_COPY_SRC;
                case VERTEX -> GpuBuffer.USAGE_VERTEX;
                case INDEX -> GpuBuffer.USAGE_INDEX;
                case UNIFORM -> GpuBuffer.USAGE_UNIFORM;
            };
        }
        return mask;
    }

    private RenderPearlConst() {}
}
