package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Hint to select the algorithm to light a {@link BakedQuad}, i.e. apply ambient occlusion and shading.
 */
public enum LightingMode {
    /**
     * Preserve the look of vanilla Minecraft. Works well for simple quads.
     */
    VANILLA,
    /**
     * Use an enhanced algorithm that provides more consistent results for complex quads.
     */
    ENHANCED,
}
