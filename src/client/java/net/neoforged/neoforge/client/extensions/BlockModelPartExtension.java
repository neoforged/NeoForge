/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockModelPartExtension {
    /**
     * Get the set {@link ChunkSectionLayer} to use when drawing this block in the level.
     * <p>
     * By default, defers query to {@link ItemBlockRenderTypes}.
     */
    default ChunkSectionLayer getRenderType(BlockState state) {
        return ItemBlockRenderTypes.getChunkRenderType(state);
    }

    /**
     * Controls the AO behavior for all quads of this model. The default behavior is to use AO unless the block emits light,
     * {@link TriState#TRUE} and {@link TriState#FALSE} force AO to be enabled and disabled respectively, regardless of
     * the block emitting light or not. {@link BakedQuad#hasAmbientOcclusion()} can be used to disable AO for a specific
     * quad even if this method says otherwise.
     * <p>
     * This method cannot force AO if the global smooth lighting video setting is disabled.
     *
     * @return {@link TriState#TRUE} to force-enable AO, {@link TriState#FALSE} to force-disable AO or {@link TriState#DEFAULT} to use vanilla AO behavior
     */
    default TriState ambientOcclusion() {
        return self().useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;
    }

    private BlockModelPart self() {
        return (BlockModelPart) this;
    }
}
